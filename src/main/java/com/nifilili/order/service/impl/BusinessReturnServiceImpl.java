package com.nifilili.order.service.impl;

import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.ReturnRequestEntity;
import com.nifilili.order.dto.request.returnflow.UpdateReturnStatusRequest;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.ReturnRequestRepository;
import com.nifilili.order.service.business.BusinessReturnService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BusinessReturnServiceImpl implements BusinessReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Allowed return status transitions.
     * This prevents illegal jumps like requested → refunded.
     */
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "requested", Set.of("pickup_scheduled", "rejected"),
            "pickup_scheduled", Set.of("picked_up", "rejected"),
            "picked_up", Set.of("received"),
            "received", Set.of("inspected"),
            "inspected", Set.of("refunded", "rejected")
    );

    /**
     * Once a return reaches these states, it is immutable.
     */
    private static final Set<String> TERMINAL_STATES = Set.of(
            "refunded",
            "rejected"
    );

    // ------------------------------------------------------------------
    // UPDATE RETURN STATUS (BUSINESS ACTION)
    // ------------------------------------------------------------------

    /**
     * Business updates return status.
     *
     * IMPORTANT:
     * - This method controls the entire return lifecycle
     * - Refund is NOT created here (RefundService handles that)
     */
    @Override
    @Transactional
    public void updateStatus(
            Long returnRequestId,
            UpdateReturnStatusRequest request
    ) {

        Long businessId = SecurityUtil.getCurrentUserId();

        // 1️⃣ Load return request
        ReturnRequestEntity returnRequest =
                returnRequestRepository.findById(returnRequestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Return request not found"));

        // 2️⃣ Load order item (to verify business ownership)
        OrderItemEntity orderItem =
                orderItemRepository.findById(returnRequest.getOrderItemId())
                        .orElseThrow(() ->
                                new IllegalStateException("Order item not found"));

        // 🔐 Ensure return belongs to this business
        if (!orderItem.getBusinessId().equals(businessId)) {
            throw new IllegalStateException("Unauthorized return update");
        }

        String currentStatus = returnRequest.getStatus();
        String newStatus = request.getStatus();

        // 🚫 Terminal state protection
        if (TERMINAL_STATES.contains(currentStatus)) {
            throw new IllegalStateException(
                    "Return already in terminal state: " + currentStatus
            );
        }

        // 🚫 Transition validation
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid return status transition from "
                            + currentStatus + " to " + newStatus
            );
        }

        // 3️⃣ Apply status-specific side effects
        applySideEffects(returnRequest, newStatus, request);

        // 4️⃣ Update core status
        returnRequest.setStatus(newStatus);
        returnRequest.setUpdatedAt(LocalDateTime.now());
        returnRequest.setUpdatedBy(businessId);

        returnRequestRepository.save(returnRequest);
    }

    // ------------------------------------------------------------------
    // INTERNAL HELPERS
    // ------------------------------------------------------------------

    /**
     * Validate lifecycle transition.
     */
    private boolean isValidTransition(String from, String to) {
        return ALLOWED_TRANSITIONS
                .getOrDefault(from, Set.of())
                .contains(to);
    }

    /**
     * Apply timestamps & validations per status.
     */
    private void applySideEffects(
            ReturnRequestEntity returnRequest,
            String newStatus,
            UpdateReturnStatusRequest request
    ) {

        LocalDateTime now = LocalDateTime.now();

        switch (newStatus) {

            case "pickup_scheduled" -> {
                returnRequest.setPickupScheduledAt(now);
            }

            case "picked_up" -> {
                // logistics pickup confirmed
            }

            case "received" -> {
                returnRequest.setReceivedAt(now);
            }

            case "inspected" -> {
                returnRequest.setInspectedAt(now);
            }

            case "rejected" -> {
                if (request.getRejectionReason() == null ||
                        request.getRejectionReason().isBlank()) {
                    throw new IllegalArgumentException(
                            "Rejection reason is mandatory"
                    );
                }
                returnRequest.setRejectedAt(now);
                returnRequest.setRejectionReason(request.getRejectionReason());
            }

            case "refunded" -> {
                /**
                 * IMPORTANT:
                 * This ONLY marks return as refund-eligible.
                 * Actual refund creation happens via RefundService.
                 */
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported return status: " + newStatus
            );
        }
    }
}

