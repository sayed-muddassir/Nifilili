package com.nifilili.order.service;

import com.nifilili.order.dto.request.CodPaymentRequest;
import com.nifilili.order.dto.request.VerifyPaymentRequest;
import com.nifilili.order.dto.response.PaymentStatusResponse;

public interface PaymentService {

    /**
     * Retrieves the payment details for an order.
     *
     * @param orderId the order ID
     * @return the payment status with type and amount
     * @throws com.nifilili.core.exception.ResourceNotFoundException if payment not found
     */
    PaymentStatusResponse getPayment(Long orderId);

    /**
     * Verifies a manual bank transfer payment (admin/business action).
     * Transitions payment status from PENDING_VERIFICATION to VERIFIED or FAILED.
     *
     * @param orderId the order ID
     * @param request verification decision and optional reference
     * @throws com.nifilili.core.exception.ResourceNotFoundException  if payment not found
     * @throws com.nifilili.core.exception.InvalidOrderStateException if payment is not in PENDING_VERIFICATION status
     */
    void verifyPayment(Long orderId, VerifyPaymentRequest request);

    /**
     * Records a cash-on-delivery payment upon delivery.
     *
     * @param orderId the order ID
     * @param request the amount received and receiver name
     * @throws com.nifilili.core.exception.ResourceNotFoundException  if payment not found
     * @throws com.nifilili.core.exception.InvalidOrderStateException if payment is not in PENDING status
     */
    void recordCodPayment(Long orderId, CodPaymentRequest request);
}
