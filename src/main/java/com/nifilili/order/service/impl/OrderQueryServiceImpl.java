package com.nifilili.order.service.impl;

import com.nifilili.core.enums.order.OrderStatus;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderPaymentEntity;
import com.nifilili.order.domain.OrderStatusHistoryEntity;
import com.nifilili.order.domain.PaymentTypeEntity;
import com.nifilili.order.dto.response.OrderDetailsResponse;
import com.nifilili.order.dto.response.OrderItemDetailsResponse;
import com.nifilili.order.dto.response.OrderSummaryResponse;
import com.nifilili.order.dto.response.OrderTimelineResponse;
import com.nifilili.order.dto.response.PaymentSummaryResponse;
import com.nifilili.order.mapper.OrderMapper;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderPaymentRepository;
import com.nifilili.order.repository.OrderRepository;
import com.nifilili.order.repository.OrderStatusHistoryRepository;
import com.nifilili.order.repository.PaymentTypeRepository;
import com.nifilili.order.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final OrderMapper orderMapper;

    @Override
    public Page<OrderSummaryResponse> listMyOrders(OrderStatus status, Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Listing orders for userId={}, status={}", userId, status);

        Page<OrderEntity> orders = (status == null)
                ? orderRepository.findByUserId(userId, pageable)
                : orderRepository.findByUserIdAndStatus(userId, status, pageable);

        return orders.map(orderMapper::toSummary);
    }

    @Override
    public OrderDetailsResponse getOrderDetails(Long orderId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Getting order details: orderId={}, userId={}", orderId, userId);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found");
        }

        // Build item details with timeline
        List<OrderItemEntity> items = orderItemRepository.findByOrderId(orderId);
        List<OrderItemDetailsResponse> itemDetails = items.stream()
                .map(item -> {
                    OrderItemDetailsResponse response = orderMapper.toItemDetails(item);

                    List<OrderStatusHistoryEntity> history =
                            statusHistoryRepository.findByOrderItemIdOrderByCreatedAtAsc(item.getId());
                    List<OrderTimelineResponse> timeline = history.stream()
                            .map(h -> OrderTimelineResponse.builder()
                                    .status(h.getNewStatus())
                                    .at(h.getCreatedAt())
                                    .note(h.getRejectionReason())
                                    .build())
                            .toList();
                    response.setTimeline(timeline);
                    return response;
                })
                .toList();

        // Build payment summary
        PaymentSummaryResponse paymentSummary = orderPaymentRepository.findByOrderId(orderId)
                .map(payment -> {
                    String typeName = paymentTypeRepository.findById(payment.getPaymentTypeId())
                            .map(PaymentTypeEntity::getName)
                            .orElse("Unknown");
                    return PaymentSummaryResponse.builder()
                            .paymentType(typeName)
                            .status(payment.getStatus().name())
                            .amount(payment.getAmount())
                            .build();
                })
                .orElse(null);

        String address = order.getAddressField1() + ", Ward " + order.getWardNumber()
                + ", " + order.getToleName();

        return OrderDetailsResponse.builder()
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .receiverName(order.getReceiverName())
                .contactNumber(order.getContactNumber())
                .email(order.getEmail())
                .address(address)
                .subtotalAmount(order.getSubtotalAmount())
                .deliveryCharge(order.getDeliveryCharge())
                .taxAmount(order.getTaxAmount())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .items(itemDetails)
                .payment(paymentSummary)
                .customerNotes(order.getCustomerNotes())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
