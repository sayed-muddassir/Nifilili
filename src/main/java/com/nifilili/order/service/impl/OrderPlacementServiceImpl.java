package com.nifilili.order.service.impl;

import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.OrderStatus;
import com.nifilili.core.enums.order.PaymentStatus;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.domain.CartEntity;
import com.nifilili.order.domain.CartItemEntity;
import com.nifilili.order.domain.CouponEntity;
import com.nifilili.order.domain.CouponUsageEntity;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderPaymentEntity;
import com.nifilili.order.domain.PaymentTypeEntity;
import com.nifilili.order.dto.request.CouponApplyRequest;
import com.nifilili.order.dto.request.PlaceOrderRequest;
import com.nifilili.order.dto.response.CartItemResponse;
import com.nifilili.order.dto.response.PlaceOrderResponse;
import com.nifilili.order.events.OrderPlacedEvent;
import com.nifilili.order.repository.CartItemRepository;
import com.nifilili.order.repository.CartRepository;
import com.nifilili.order.repository.CouponUsageRepository;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderPaymentRepository;
import com.nifilili.order.repository.OrderRepository;
import com.nifilili.order.repository.PaymentTypeRepository;
import com.nifilili.order.service.BusinessConfigService;
import com.nifilili.order.service.CartEnrichmentService;
import com.nifilili.order.service.CouponService;
import com.nifilili.order.service.OrderPlacementService;
import com.nifilili.order.service.PricingService;
import com.nifilili.order.service.PricingService.PricingResult;
import com.nifilili.order.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderPlacementServiceImpl implements OrderPlacementService {

    private static final String COD_PAYMENT_TYPE = "CASH_ON_DELIVERY";

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CartEnrichmentService cartEnrichmentService;
    private final CouponService couponService;
    private final BusinessConfigService businessConfigService;
    private final PricingService pricingService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Placing order for userId={}", userId);
        LocalDateTime now = LocalDateTime.now();

        // 1. Get cart
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new InvalidOrderStateException("Cart not found"));
        List<CartItemEntity> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new InvalidOrderStateException("Cart is empty");
        }

        // 2. Enrich cart items
        List<CartItemResponse> enrichedItems = cartEnrichmentService.enrich(cartItems);

        // 3. Validate payment type
        PaymentTypeEntity paymentType = paymentTypeRepository.findById(request.getPaymentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment type not found"));

        // 4. Resolve coupons
        Map<Long, CouponEntity> coupons = resolveCoupons(request.getCoupons(), userId);

        // 5. Get business configs
        Set<Long> businessIds = enrichedItems.stream()
                .map(CartItemResponse::getBusinessId)
                .collect(Collectors.toSet());
        Map<Long, Map<String, String>> businessConfigs = businessConfigService.getConfigMap(businessIds);

        // 6. Calculate pricing
        PricingResult pricing = pricingService.calculate(enrichedItems, coupons, businessConfigs);

        // 7. Create order
        OrderEntity order = OrderEntity.builder()
                .userId(userId)
                .orderNumber(OrderNumberGenerator.generateOrderNumber())
                .invoiceNumber(OrderNumberGenerator.generateInvoiceNumber())
                .receiverName(request.getReceiverName())
                .contactNumber(request.getContactNumber())
                .email(request.getEmail())
                .municipalityId(request.getAddress().getMunicipalityId())
                .wardNumber(request.getAddress().getWardNumber())
                .toleName(request.getAddress().getToleName())
                .addressField1(request.getAddress().getAddressField1())
                .postalCode(request.getAddress().getPostalCode() != null
                        ? request.getAddress().getPostalCode() : "")
                .subtotalAmount(pricing.subtotal())
                .deliveryCharge(pricing.totalDelivery())
                .taxAmount(pricing.totalTax())
                .discountAmount(pricing.totalDiscount())
                .totalAmount(pricing.payableAmount())
                .status(OrderStatus.PLACED)
                .paymentStatus(PaymentStatus.PENDING)
                .amountPaid(BigDecimal.ZERO)
                .customerNotes(request.getCustomerNotes())
                .isB2bOrder(false)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(userId)
                .updatedBy(userId)
                .build();
        order = orderRepository.save(order);

        // 8. Create order items
        for (CartItemResponse item : enrichedItems) {
            Long itemId = item.getCartItemId();
            CouponEntity coupon = coupons.get(item.getBusinessId());

            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .orderId(order.getId())
                    .businessId(item.getBusinessId())
                    .offeringId(item.getOfferingId())
                    .couponId(coupon != null ? coupon.getId() : null)
                    .variantId(item.getVariantId())
                    .title(item.getTitle())
                    .sku("SKU-" + item.getOfferingId()) // TODO: get from offering
                    .variantAttributes(item.getVariantAttributes())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .discountAmount(pricing.itemDiscounts().getOrDefault(itemId, BigDecimal.ZERO))
                    .deliveryCharge(pricing.itemDelivery().getOrDefault(itemId, BigDecimal.ZERO))
                    .taxAmount(pricing.itemTaxes().getOrDefault(itemId, BigDecimal.ZERO))
                    .subtotal(pricing.itemSubtotals().getOrDefault(itemId, BigDecimal.ZERO))
                    .status(OrderItemStatus.PLACED)
                    .createdAt(now)
                    .updatedAt(now)
                    .createdBy(userId)
                    .updatedBy(userId)
                    .build();
            orderItemRepository.save(orderItem);
        }

        // 9. Create payment
        PaymentStatus initialPaymentStatus = paymentType.getName().equals(COD_PAYMENT_TYPE)
                ? PaymentStatus.PENDING
                : PaymentStatus.PENDING_VERIFICATION;

        OrderPaymentEntity payment = OrderPaymentEntity.builder()
                .orderId(order.getId())
                .paymentTypeId(paymentType.getId())
                .amount(pricing.payableAmount())
                .status(initialPaymentStatus)
                .paymentDetails(request.getPaymentDetails() != null
                        ? request.getPaymentDetails() : Map.of())
                .createdAt(now)
                .createdBy(userId)
                .updatedAt(now)
                .updatedBy(userId)
                .build();
        orderPaymentRepository.save(payment);

        order.setPaymentStatus(initialPaymentStatus);
        orderRepository.save(order);

        // 10. Record coupon usage
        for (Map.Entry<Long, CouponEntity> entry : coupons.entrySet()) {
            CouponUsageEntity usage = CouponUsageEntity.builder()
                    .couponId(entry.getValue().getId())
                    .userId(userId)
                    .orderId(order.getId())
                    .usedAt(now)
                    .build();
            couponUsageRepository.save(usage);
        }

        // 11. Clear cart
        cartItemRepository.deleteByCartId(cart.getId());

        // 12. Publish event
        eventPublisher.publishEvent(new OrderPlacedEvent(order.getId(), userId, order.getOrderNumber()));
        log.info("Order placed: orderId={}, orderNumber={}", order.getId(), order.getOrderNumber());

        return PlaceOrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .paymentStatus(initialPaymentStatus.name())
                .totalAmount(pricing.payableAmount())
                .build();
    }

    private Map<Long, CouponEntity> resolveCoupons(List<CouponApplyRequest> couponRequests, Long userId) {
        Map<Long, CouponEntity> coupons = new HashMap<>();
        if (couponRequests == null || couponRequests.isEmpty()) {
            return coupons;
        }
        for (CouponApplyRequest req : couponRequests) {
            CouponEntity coupon = couponService.validateAndGet(req.getBusinessId(), req.getCouponCode(), userId);
            coupons.put(req.getBusinessId(), coupon);
        }
        return coupons;
    }
}
