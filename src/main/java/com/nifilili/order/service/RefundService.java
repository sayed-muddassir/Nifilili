package com.nifilili.order.service;

import com.nifilili.order.dto.request.CreateRefundRequest;
import com.nifilili.order.dto.request.UpdateRefundStatusRequest;
import com.nifilili.order.dto.response.RefundResponse;

public interface RefundService {

    /**
     * Creates a refund request for an order with bank account details.
     *
     * @param orderId the order ID to refund
     * @param request the bank account details for the refund
     * @return the created refund response
     * @throws com.nifilili.core.exception.ResourceNotFoundException if order not found
     */
    RefundResponse createRefund(Long orderId, CreateRefundRequest request);

    /**
     * Updates the status of a refund (PENDING → APPROVED → PROCESSED or FAILED).
     * On PROCESSED: publishes RefundProcessedEvent.
     *
     * @param refundId the refund ID
     * @param request  the new status and optional reference
     * @return the updated refund response
     * @throws com.nifilili.core.exception.ResourceNotFoundException  if refund not found
     * @throws com.nifilili.core.exception.InvalidOrderStateException if the status transition is not allowed
     */
    RefundResponse updateRefundStatus(Long refundId, UpdateRefundStatusRequest request);
}
