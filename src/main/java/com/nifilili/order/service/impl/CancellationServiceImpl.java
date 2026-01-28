package com.nifilili.order.service.impl;

import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.dto.request.cancellation.CancellationDecisionRequest;
import com.nifilili.order.dto.request.cancellation.CancellationItemRequest;
import com.nifilili.order.dto.request.cancellation.CancellationRequest;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderStatusHistoryEntity;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderStatusHistoryRepository;
import com.nifilili.order.service.cancellation.CancellationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CancellationServiceImpl implements CancellationService {

    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    /**
     * States where cancellation is no longer allowed.
     */
    private static final Set<String> NON_CANCELLABLE_STATES = Set.of(
            "shipped",
            "delivered",
            "rejected",
            "cancelled",
            "returned"
    );

    // ------------------------------------------------------------------
    // USER SIDE – REQUEST CANCELLATION
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public void requestCancellation(Long orderId, CancellationRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();

        for (CancellationItemRequest itemRequest : request.getItems()) {

            OrderItemEntity item = orderItemRepository.findById(itemRequest.getOrderItemId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Order item not found"));

            // 🔐 Ensure item belongs to this order
            if (!item.getOrderId().equals(orderId)) {
                throw new IllegalStateException("Order item does not belong to order");
            }

            // 🚫 Check cancellation eligibility
            if (NON_CANCELLABLE_STATES.contains(item.getStatus())) {
                throw new IllegalStateException(
                        "Item cannot be cancelled in state: " + item.getStatus()
                );
            }

            /**
             * IMPORTANT:
             * We DO NOT change item status here.
             * This is only a request – business must approve/reject.
             *
             * In a real system you might:
             * - persist a cancellation_request table
             * - notify business
             *
             * For now, we rely on business decision API.
             */

            // (Optional) Log request intent via history
            OrderStatusHistoryEntity history = new OrderStatusHistoryEntity();
            history.setOrderItemId(item.getId());
            history.setOldStatus(item.getStatus());
            history.setNewStatus("cancellation_requested");
            history.setRejectionReason(itemRequest.getReason());
            history.setCreatedAt(LocalDateTime.now());
            history.setCreatedBy(userId);

            orderStatusHistoryRepository.save(history);
        }
    }

    // ------------------------------------------------------------------
    // BUSINESS SIDE – DECIDE CANCELLATION
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public void decide(Long orderItemId, CancellationDecisionRequest request) {

        Long businessId = SecurityUtil.getCurrentUserId();

        OrderItemEntity item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order item not found"));

        // 🔐 Business ownership check
        if (!item.getBusinessId().equals(businessId)) {
            throw new IllegalStateException("Unauthorized cancellation decision");
        }

        // 🚫 Already terminal
        if (Set.of("cancelled", "rejected", "returned").contains(item.getStatus())) {
            return;
        }

        if (request.getApproved()) {
            approveCancellation(item, businessId);
        } else {
            rejectCancellation(item, request, businessId);
        }
    }

    // ------------------------------------------------------------------
    // INTERNAL HELPERS
    // ------------------------------------------------------------------

    private void approveCancellation(OrderItemEntity item, Long businessId) {

        // 🚫 Cannot cancel shipped/delivered
        if (NON_CANCELLABLE_STATES.contains(item.getStatus())) {
            throw new IllegalStateException(
                    "Cannot cancel item in state: " + item.getStatus()
            );
        }

        // 1️⃣ History
        OrderStatusHistoryEntity history = new OrderStatusHistoryEntity();
        history.setOrderItemId(item.getId());
        history.setOldStatus(item.getStatus());
        history.setNewStatus("cancelled");
        history.setCreatedAt(LocalDateTime.now());
        history.setCreatedBy(businessId);

        orderStatusHistoryRepository.save(history);

        // 2️⃣ Update item
        item.setStatus("cancelled");
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(businessId);

        orderItemRepository.save(item);

        /**
         * IMPORTANT:
         * Refund is NOT triggered here.
         *
         * Refund eligibility depends on:
         * - payment status
         * - payment type
         *
         * RefundService will handle it explicitly.
         */
    }

    private void rejectCancellation(
            OrderItemEntity item,
            CancellationDecisionRequest request,
            Long businessId
    ) {

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException(
                    "Rejection reason is mandatory"
            );
        }

        // 1️⃣ History
        OrderStatusHistoryEntity history = new OrderStatusHistoryEntity();
        history.setOrderItemId(item.getId());
        history.setOldStatus(item.getStatus());
        history.setNewStatus(item.getStatus()); // remains same
        history.setRejectionReason(request.getReason());
        history.setCreatedAt(LocalDateTime.now());
        history.setCreatedBy(businessId);

        orderStatusHistoryRepository.save(history);

        // ❗ Item status does NOT change
    }
}
