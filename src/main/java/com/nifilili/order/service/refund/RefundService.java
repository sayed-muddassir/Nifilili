package com.nifilili.order.service.refund;

import com.nifilili.order.dto.request.refund.CreateRefundRequest;
import com.nifilili.order.dto.request.refund.UpdateRefundStatusRequest;
import com.nifilili.order.dto.response.refund.RefundResponse;

public interface RefundService {

    /**
     * Create a refund for an order.
     *
     * Refund is created ONLY for:
     *  - cancelled items (approved)
     *  - returned items (approved after inspection)
     *
     * Refund amount is calculated internally.
     */
    RefundResponse createRefund(Long orderId, CreateRefundRequest request);

    /**
     * Update refund status (admin / finance action).
     */
    void updateRefundStatus(Long refundId, UpdateRefundStatusRequest request);
}
