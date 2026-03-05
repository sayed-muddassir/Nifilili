package com.nifilili.order.service.impl;

import com.nifilili.core.enums.order.CancellationStatus;
import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.PaymentStatus;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.domain.CancellationRequestEntity;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderStatusHistoryEntity;
import com.nifilili.order.dto.request.CancellationDecisionRequest;
import com.nifilili.order.dto.request.CancellationItemRequest;
import com.nifilili.order.dto.request.CancellationRequest;
import com.nifilili.order.dto.response.CancellationResponse;
import com.nifilili.order.events.CancellationApprovedEvent;
import com.nifilili.order.repository.CancellationRequestRepository;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderRepository;
import com.nifilili.order.repository.OrderStatusHistoryRepository;
import com.nifilili.order.service.CancellationService;
import com.nifilili.order.util.OrderStatusDeriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CancellationServiceImpl implements CancellationService {

    private static final Set<OrderItemStatus> CANCELLABLE_STATUSES = Set.of(
            OrderItemStatus.PLACED, OrderItemStatus.RECEIVED
    );

    private final CancellationRequestRepository cancellationRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<CancellationResponse> requestCancellation(Long orderId, CancellationRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Requesting cancellation for orderId={}, items={}", orderId, request.getItems().size());
        LocalDateTime now = LocalDateTime.now();

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found");
        }

        List<CancellationResponse> responses = new ArrayList<>();

        for (CancellationItemRequest itemReq : request.getItems()) {
            OrderItemEntity item = orderItemRepository.findById(itemReq.getOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Order item not found: " + itemReq.getOrderItemId()));

            if (!item.getOrderId().equals(orderId)) {
                throw new InvalidOrderStateException("Order item does not belong to this order");
            }

            if (!CANCELLABLE_STATUSES.contains(item.getStatus())) {
                throw new InvalidOrderStateException(
                        "Cannot cancel item in status: " + item.getStatus());
            }

            if (cancellationRepository.existsByOrderItemIdAndStatus(
                    item.getId(), CancellationStatus.PENDING)) {
                throw new InvalidOrderStateException("Cancellation already pending for item");
            }

            CancellationRequestEntity cancellation = CancellationRequestEntity.builder()
                    .orderItemId(item.getId())
                    .reason(itemReq.getReason())
                    .note(itemReq.getNote())
                    .status(CancellationStatus.PENDING)
                    .createdAt(now)
                    .createdBy(userId)
                    .build();
            cancellationRepository.save(cancellation);

            responses.add(CancellationResponse.builder()
                    .orderItemId(item.getId())
                    .status(CancellationStatus.PENDING.name())
                    .reason(itemReq.getReason())
                    .build());
        }

        return responses;
    }

    @Override
    public CancellationResponse decide(Long orderItemId, CancellationDecisionRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Deciding cancellation for orderItemId={}, approved={}", orderItemId, request.getApproved());
        LocalDateTime now = LocalDateTime.now();

        CancellationRequestEntity cancellation = cancellationRepository.findByOrderItemId(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancellation request not found"));

        if (cancellation.getStatus() != CancellationStatus.PENDING) {
            throw new InvalidOrderStateException("Cancellation is not in PENDING status");
        }

        OrderItemEntity item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        if (request.getApproved()) {
            cancellation.setStatus(CancellationStatus.APPROVED);
            cancellation.setDecidedBy(userId);
            cancellation.setDecidedAt(now);
            cancellation.setDecisionReason(request.getReason());
            cancellationRepository.save(cancellation);

            // Update item status
            OrderItemStatus oldStatus = item.getStatus();
            item.setStatus(OrderItemStatus.CANCELLED);
            item.setUpdatedAt(now);
            item.setUpdatedBy(userId);
            orderItemRepository.save(item);

            // Record history
            statusHistoryRepository.save(OrderStatusHistoryEntity.builder()
                    .orderItemId(item.getId())
                    .oldStatus(oldStatus.name())
                    .newStatus(OrderItemStatus.CANCELLED.name())
                    .rejectionReason("Cancellation approved")
                    .createdBy(userId)
                    .createdAt(now)
                    .build());

            // Re-derive order status
            rederiveOrderStatus(item.getOrderId());

            // Check if refund is needed (order was paid)
            OrderEntity order = orderRepository.findById(item.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            if (order.getPaymentStatus() == PaymentStatus.COMPLETED
                    || order.getPaymentStatus() == PaymentStatus.VERIFIED) {
                BigDecimal refundable = item.getSubtotal()
                        .subtract(item.getDiscountAmount())
                        .add(item.getTaxAmount());
                eventPublisher.publishEvent(new CancellationApprovedEvent(
                        item.getOrderId(), item.getId(), refundable));
            }
        } else {
            cancellation.setStatus(CancellationStatus.REJECTED);
            cancellation.setDecidedBy(userId);
            cancellation.setDecidedAt(now);
            cancellation.setDecisionReason(request.getReason());
            cancellationRepository.save(cancellation);
        }

        return CancellationResponse.builder()
                .orderItemId(orderItemId)
                .status(cancellation.getStatus().name())
                .reason(cancellation.getReason())
                .build();
    }

    private void rederiveOrderStatus(Long orderId) {
        List<OrderItemEntity> items = orderItemRepository.findByOrderId(orderId);
        var derivedStatus = OrderStatusDeriver.derive(items);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() != derivedStatus) {
            order.setStatus(derivedStatus);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
    }
}
