package com.nifilili.order.service.payment;

import com.nifilili.order.dto.request.payment.CodPaymentRequest;
import com.nifilili.order.dto.request.payment.VerifyPaymentRequest;
import com.nifilili.order.dto.response.payment.PaymentStatusResponse;

public interface PaymentService {

    /**
     * Get payment details for an order.
     * Used by customer & business.
     */
    PaymentStatusResponse getPayment(Long orderId);

    /**
     * Verify manual / online payment.
     * Business or admin action.
     */
    void verifyPayment(Long orderId, VerifyPaymentRequest request);

    /**
     * Record Cash-On-Delivery payment after delivery.
     */
    void recordCodPayment(Long orderId, CodPaymentRequest request);
}
