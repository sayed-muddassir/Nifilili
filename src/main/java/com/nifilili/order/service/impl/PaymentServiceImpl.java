package com.nifilili.order.service.impl;

import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.dto.request.payment.CodPaymentRequest;
import com.nifilili.order.dto.request.payment.VerifyPaymentRequest;
import com.nifilili.order.dto.response.payment.PaymentStatusResponse;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderPaymentEntity;
import com.nifilili.order.repository.OrderPaymentRepository;
import com.nifilili.order.repository.OrderRepository;
import com.nifilili.order.service.payment.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final OrderPaymentRepository orderPaymentRepository;

    // ------------------------------------------------------------
    // GET PAYMENT STATUS
    // ------------------------------------------------------------

    @Override
    public PaymentStatusResponse getPayment(Long orderId) {

        Long userId = SecurityUtil.getCurrentUserId();

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found"));

        // 🔐 Ownership check (customer view)
        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("Unauthorized access to payment");
        }

        OrderPaymentEntity payment = orderPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new IllegalStateException("Payment record not found"));

        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setPaymentTypeId(payment.getPaymentTypeId());
        response.setStatus(payment.getStatus());
        response.setAmount(payment.getAmount());
        response.setPaymentDetails(payment.getPaymentDetails());

        return response;
    }

    // ------------------------------------------------------------
    // VERIFY MANUAL / ONLINE PAYMENT
    // ------------------------------------------------------------

    /**
     * Called by business or admin after verifying payment proof.
     */
    @Override
    @Transactional
    public void verifyPayment(Long orderId, VerifyPaymentRequest request) {

        Long verifierId = SecurityUtil.getCurrentUserId();

        OrderPaymentEntity payment = orderPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new IllegalStateException("Payment record not found"));

        // 🚫 Already verified / completed
        if ("verified".equalsIgnoreCase(payment.getStatus()) ||
                "completed".equalsIgnoreCase(payment.getStatus())) {
            return;
        }

        if (Boolean.TRUE.equals(request.getVerified())) {

            payment.setStatus("verified");

            // store reference if provided
            if (request.getReference() != null) {
                payment.setPaymentDetails(
                        merge(payment.getPaymentDetails(),
                                Map.of("verification_reference", request.getReference()))
                );
            }

            payment.setUpdatedAt(LocalDateTime.now());
            payment.setUpdatedBy(verifierId);

            orderPaymentRepository.save(payment);

            // Sync order payment status
            markOrderAsPaid(orderId, payment.getAmount(), verifierId);

        } else {
            payment.setStatus("failed");
            payment.setUpdatedAt(LocalDateTime.now());
            payment.setUpdatedBy(verifierId);
            orderPaymentRepository.save(payment);
        }
    }

    // ------------------------------------------------------------
    // RECORD COD PAYMENT
    // ------------------------------------------------------------

    /**
     * Called when delivery agent collects cash.
     */
    @Override
    @Transactional
    public void recordCodPayment(Long orderId, CodPaymentRequest request) {

        Long collectorId = SecurityUtil.getCurrentUserId();

        OrderPaymentEntity payment = orderPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new IllegalStateException("Payment record not found"));

        // 🚫 COD only
        if (!isCodPayment(payment)) {
            throw new IllegalStateException("Not a COD order");
        }

        // 🚫 Already completed
        if ("completed".equalsIgnoreCase(payment.getStatus())) {
            return;
        }

        payment.setStatus("completed");
        payment.setPaymentDetails(
                merge(payment.getPaymentDetails(),
                        Map.of(
                                "amount_received", request.getAmountReceived(),
                                "receiver_name", request.getReceiverName(),
                                "delivery_agent", request.getDeliveryAgent()
                        ))
        );
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setUpdatedBy(collectorId);

        orderPaymentRepository.save(payment);

        // Sync order payment status
        markOrderAsPaid(orderId, request.getAmountReceived(), collectorId);
    }

    // ------------------------------------------------------------
    // INTERNAL HELPERS
    // ------------------------------------------------------------

    private void markOrderAsPaid(
            Long orderId,
            BigDecimal amountPaid,
            Long actorId
    ) {

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalStateException("Order not found"));

        order.setPaymentStatus("paid");
        order.setAmountPaid(amountPaid);
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(actorId);

        orderRepository.save(order);
    }

    private boolean isCodPayment(OrderPaymentEntity payment) {
        // Assuming payment_type_id for COD is known (e.g. 1)
        return payment.getPaymentTypeId() == 1L;
    }

    /**
     * Utility to merge JSON-like maps safely.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> merge(
            Map<String, Object> original,
            Map<String, Object> additions
    ) {

        original.putAll(additions);
        return original;
    }
}

