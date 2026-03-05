package com.nifilili.order.service;

import com.nifilili.order.dto.request.CancellationDecisionRequest;
import com.nifilili.order.dto.request.CancellationRequest;
import com.nifilili.order.dto.response.CancellationResponse;

import java.util.List;

public interface CancellationService {

    /**
     * Requests cancellation for one or more order items. Creates a cancellation request
     * per item with PENDING status.
     *
     * @param orderId the order ID
     * @param request the cancellation request with item-level reasons
     * @return list of cancellation responses
     * @throws com.nifilili.core.exception.ResourceNotFoundException  if order not found
     * @throws com.nifilili.core.exception.InvalidOrderStateException if any item is not in a cancellable state
     */
    List<CancellationResponse> requestCancellation(Long orderId, CancellationRequest request);

    /**
     * Approves or rejects a cancellation request. On approval: sets item status to CANCELLED,
     * re-derives order status, and publishes CancellationApprovedEvent if payment was made.
     *
     * @param orderItemId the order item ID with a pending cancellation
     * @param request     the approval/rejection decision with optional reason
     * @return the updated cancellation response
     * @throws com.nifilili.core.exception.ResourceNotFoundException  if cancellation request not found
     * @throws com.nifilili.core.exception.InvalidOrderStateException if no pending cancellation exists
     */
    CancellationResponse decide(Long orderItemId, CancellationDecisionRequest request);
}
