package com.nifilili.order.service.impl;

import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderPaymentEntity;
import com.nifilili.order.domain.OrderStatusHistoryEntity;
import com.nifilili.order.dto.request.orderplacement.PlaceOrderRequest;
import com.nifilili.order.dto.response.orderplacement.*;
import com.nifilili.order.factory.PricingContextFactory;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.model.*;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderPaymentRepository;
import com.nifilili.order.repository.OrderRepository;
import com.nifilili.order.repository.OrderStatusHistoryRepository;
import com.nifilili.order.service.cart.CartService;
import com.nifilili.order.service.business.PricingService;
import com.nifilili.order.service.orderplacement.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartService cartService;
    private final PricingService pricingService;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    // ------------------------------------------------------------
    // ORDER PLACEMENT (ATOMIC)
    // ------------------------------------------------------------

    /**
     * This method is the HEART of the order system.
     *
     * If ANY step fails → NOTHING is committed.
     */
    @Override
    @Transactional
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();

        // 1️⃣ Snapshot & validate cart
        CartSnapshot cartSnapshot = cartService.getValidatedCartSnapshot();

        // 2️⃣ Calculate pricing again (never trust frontend)
        PricingContext pricingContext =
                PricingContextFactory.from(cartSnapshot, request);

        PricingResult pricingResult =
                pricingService.calculate(pricingContext);

        // 3️⃣ Create Order (parent)
        OrderEntity order = createOrder(userId, request, pricingResult);
        orderRepository.save(order);

        // 4️⃣ Create Order Items (snapshot per business/product)
        createOrderItems(order, cartSnapshot, pricingResult, userId);

        // 5️⃣ Create initial payment record
        createPayment(order, request, userId);

        // 6️⃣ Clear cart AFTER successful order creation
        cartService.clearCart();

        return new PlaceOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getPaymentStatus(),
                order.getTotalAmount()
        );
    }

    // ------------------------------------------------------------
    // LIST USER ORDERS
    // ------------------------------------------------------------

    @Override
    public Page<OrderSummaryResponse> listMyOrders(
            String status,
            Pageable pageable
    ) {

        Long userId = SecurityUtil.getCurrentUserId();

        Page<OrderEntity> orders;

        if (status == null) {
            orders = orderRepository.findByUserId(userId, pageable);
        } else {
            orders = orderRepository.findByUserIdAndStatus(
                    userId, status, pageable
            );
        }

        return orders.map(this::mapToSummaryResponse);
    }

    // ------------------------------------------------------------
    // GET ORDER DETAILS
    // ------------------------------------------------------------

    @Override
    public OrderDetailsResponse getOrderDetails(Long orderId) {

        Long userId = SecurityUtil.getCurrentUserId();

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found"));

        // 🔐 Ownership check
        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("Unauthorized access to order");
        }

        List<OrderItemEntity> items =
                orderItemRepository.findByOrderId(orderId);

        List<OrderItemDetailsResponse> itemResponses =
                items.stream()
                        .map(this::mapToItemDetailsResponse)
                        .toList();

        OrderDetailsResponse response = new OrderDetailsResponse();
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus());
        response.setItems(itemResponses);

        return response;
    }

    // ------------------------------------------------------------
    // INTERNAL HELPERS
    // ------------------------------------------------------------

    private OrderEntity createOrder(
            Long userId,
            PlaceOrderRequest request,
            PricingResult pricing
    ) {

        OrderEntity order = new OrderEntity();

        order.setUserId(userId);
        order.setOrderNumber(generateOrderNumber());
        order.setInvoiceNumber(generateInvoiceNumber());

        // Address snapshot
        order.setReceiverName(request.getReceiverName());
        order.setContactNumber(request.getContactNumber());
        order.setEmail(request.getEmail());
        order.setMunicipalityId(request.getAddress().getMunicipalityId());
        order.setWardNumber(request.getAddress().getWardNumber());
        order.setToleName(request.getAddress().getToleName());
        order.setAddressField1(request.getAddress().getAddressField1());
        order.setPostalCode(request.getAddress().getPostalCode());

        // Amounts
        order.setSubtotalAmount(pricing.getSummary().getSubTotal());
        order.setTaxAmount(pricing.getSummary().getTotalTax());
        order.setDeliveryCharge(pricing.getSummary().getDeliveryCharge());
        order.setDiscountAmount(pricing.getSummary().getTotalDiscount());
        order.setTotalAmount(pricing.getSummary().getPayableAmount());

        // Status
        order.setStatus("placed");
        order.setPaymentStatus("pending");
        order.setAmountPaid(BigDecimal.ZERO);
        order.setCustomerNotes("TODO from request");
        order.setIsB2bOrder(false);
        order.setB2bQuoteId(0L);
        order.setSource("TODO Source");
        order.setSourceQuoteId(0L);
        order.setSourceRequestId(0L);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setCreatedBy(userId);
        order.setUpdatedBy(userId);

        return order;
    }

    private void createOrderItems(
            OrderEntity order,
            CartSnapshot cartSnapshot,
            PricingResult pricingResult,
            Long userId
    ) {

        Map<Long, PricingItem> pricingByOffering =
                pricingResult.getItems().stream()
                        .collect(Collectors.toMap(
                                PricingItem::getOfferingId,
                                p -> p
                        ));

        for (CartItemSnapshot cartItem : cartSnapshot.getItems()) {

            PricingItem pricingItem =
                    pricingByOffering.get(cartItem.getOfferingId());

            OrderItemEntity item = new OrderItemEntity();

            item.setOrderId(order.getId());
            item.setBusinessId(cartItem.getBusinessId());
            item.setOfferingId(cartItem.getOfferingId());
            item.setVariantId(cartItem.getVariantId());

            // Snapshot fields
            item.setTitle("Snapshot Title"); // from Offering service
            item.setSku("SKU-001");
            item.setVariantAttributes(Map.of());

            item.setQuantity(cartItem.getQuantity());
            item.setUnitPrice(pricingItem.getUnitPrice());
            item.setSubtotal(pricingItem.getSubTotal());
            item.setTaxAmount(pricingItem.getTax());
            item.setDeliveryCharge(pricingItem.getDeliveryCharge());
            item.setDiscountAmount(pricingItem.getDiscount());

            item.setStatus("placed");

            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            item.setCreatedBy(userId);
            item.setUpdatedBy(userId);

            orderItemRepository.save(item);

            // Initial status history
            OrderStatusHistoryEntity history = new OrderStatusHistoryEntity();
            history.setOrderItemId(item.getId());
            history.setOldStatus("todo order created first time");
            history.setNewStatus("placed");
            history.setRejectionReason("NA");
            history.setCreatedAt(LocalDateTime.now());
            history.setCreatedBy(userId);

            orderStatusHistoryRepository.save(history);
        }
    }

    private void createPayment(
            OrderEntity order,
            PlaceOrderRequest request,
            Long userId
    ) {

        OrderPaymentEntity payment = new OrderPaymentEntity();

        payment.setOrderId(order.getId());
        payment.setPaymentTypeId(request.getPaymentTypeId());
        payment.setAmount(order.getTotalAmount());
        payment.setStatus("pending");
        payment.setPaymentDetails(request.getPaymentDetails());

        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setCreatedBy(userId);
        payment.setUpdatedBy(userId);

        orderPaymentRepository.save(payment);
    }

    private OrderSummaryResponse mapToSummaryResponse(OrderEntity order) {

        OrderSummaryResponse response = new OrderSummaryResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());

        return response;
    }

    private OrderItemDetailsResponse mapToItemDetailsResponse(
            OrderItemEntity item
    ) {

        OrderItemDetailsResponse response = new OrderItemDetailsResponse();
        response.setOrderItemId(item.getId());
        response.setBusinessId(item.getBusinessId());
        response.setTitle(item.getTitle());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setSubtotal(item.getSubtotal());
        response.setStatus(item.getStatus());

        // Timeline
        List<OrderTimelineResponse> timeline =
                orderStatusHistoryRepository
                        .findByOrderItemIdOrderByCreatedAtAsc(item.getId())
                        .stream()
                        .map(h -> {
                            OrderTimelineResponse t = new OrderTimelineResponse();
                            t.setStatus(h.getNewStatus());
                            t.setAt(h.getCreatedAt());
                            t.setNote(h.getRejectionReason());
                            return t;
                        })
                        .toList();

        response.setTimeline(timeline);
        return response;
    }

    // ------------------------------------------------------------
    // ID GENERATORS
    // ------------------------------------------------------------

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }
}
