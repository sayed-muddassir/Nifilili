package com.nifilili.order.service.impl;

import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.dto.request.refund.CreateRefundRequest;
import com.nifilili.order.dto.request.refund.UpdateRefundStatusRequest;
import com.nifilili.order.dto.response.refund.RefundResponse;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderRefundEntity;
import com.nifilili.order.domain.RefundItemEntity;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderRefundRepository;
import com.nifilili.order.repository.RefundItemRepository;
import com.nifilili.order.service.refund.RefundService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRefundRepository orderRefundRepository;
    private final RefundItemRepository refundItemRepository;

    /**
     * Item states eligible for refund.
     */
    private static final Set<String> REFUND_ELIGIBLE_ITEM_STATES = Set.of(
            "cancelled",
            "return_approved",
            "returned",
            "refunded"
    );

    /**
     * Allowed refund status transitions.
     */
    private static final Set<String> ALLOWED_REFUND_STATUSES = Set.of(
            "pending",
            "approved",
            "processed",
            "failed",
            "cancelled"
    );

    // ------------------------------------------------------------------
    // CREATE REFUND
    // ------------------------------------------------------------------

    /**
     * Create a refund for an order.
     *
     * IMPORTANT RULES:
     * - Refunds are NEVER auto-created.
     * - Only approved items are refunded.
     * - Supports partial refunds.
     */
    @Override
    @Transactional
    public RefundResponse createRefund(Long orderId, CreateRefundRequest request) {

        Long actorId = SecurityUtil.getCurrentUserId();

        // 1️⃣ Fetch refundable items
        List<OrderItemEntity> refundableItems =
                orderItemRepository.findByOrderId(orderId).stream()
                        .filter(item ->
                                REFUND_ELIGIBLE_ITEM_STATES.contains(item.getStatus()))
                        .toList();

        if (refundableItems.isEmpty()) {
            throw new IllegalStateException(
                    "No refundable items found for order"
            );
        }

        // 2️⃣ Calculate refund amount (item-level)
        BigDecimal refundAmount = refundableItems.stream()
                .map(this::calculateRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3️⃣ Create refund record
        OrderRefundEntity refund = new OrderRefundEntity();
        refund.setOrderId(orderId);
        refund.setRefundAmount(refundAmount);
        refund.setStatus("pending");

        refund.setBankName(request.getBankName());
        refund.setBankAccountName(request.getAccountHolder());
        refund.setAccountNumber(request.getAccountNumber());
        refund.setBranch(request.getBranch());

        refund.setProcessedAt(null);

        orderRefundRepository.save(refund);

        // 4️⃣ Create refund items (one per order item)
        for (OrderItemEntity item : refundableItems) {

            RefundItemEntity refundItem = new RefundItemEntity();
            refundItem.setRefundId(refund.getId());
            refundItem.setOrderItemId(item.getId());
//            refundItem.setRefundAmount(calculateRefundAmount(item));
//            refundItem.setStatus("pending"); TOD UNCOMMENT if needed in future

            refundItemRepository.save(refundItem);
        }

        return new RefundResponse(
                refund.getId(),
                refundAmount,
                refund.getStatus()
        );
    }

    // ------------------------------------------------------------------
    // UPDATE REFUND STATUS
    // ------------------------------------------------------------------

    /**
     * Update refund status.
     *
     * Typical flow:
     * pending → approved → processed
     */
    @Override
    @Transactional
    public void updateRefundStatus(
            Long refundId,
            UpdateRefundStatusRequest request
    ) {

        Long actorId = SecurityUtil.getCurrentUserId();

        OrderRefundEntity refund = orderRefundRepository.findById(refundId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Refund not found"));

        String newStatus = request.getStatus();

        // 🚫 Validate status
        if (!ALLOWED_REFUND_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid refund status: " + newStatus
            );
        }

        // 🚫 Terminal protection
        if (isTerminal(refund.getStatus())) {
            throw new IllegalStateException(
                    "Refund already in terminal state: " + refund.getStatus()
            );
        }

        // 1️⃣ Update refund record
        refund.setStatus(newStatus);

        if ("processed".equalsIgnoreCase(newStatus)) {
            refund.setRefundReference(request.getReference());
            refund.setProcessedAt(LocalDateTime.now());
        }

        orderRefundRepository.save(refund);

        // 2️⃣ Sync refund items
        refundItemRepository.findAll().stream()
                .filter(ri -> ri.getRefundId().equals(refundId))
                .forEach(ri -> {
//                    ri.setStatus(newStatus);
                    refundItemRepository.save(ri);
                });
    }

    // ------------------------------------------------------------------
    // INTERNAL HELPERS
    // ------------------------------------------------------------------

    /**
     * Refund amount calculation logic.
     *
     * Business rule:
     * refund = subtotal + tax + delivery - discount
     */
    private BigDecimal calculateRefundAmount(OrderItemEntity item) {

        return item.getSubtotal()
                .add(item.getTaxAmount())
                .add(item.getDeliveryCharge())
                .subtract(item.getDiscountAmount());
    }

    private boolean isTerminal(String status) {
        return Set.of("processed", "cancelled").contains(status);
    }
}
