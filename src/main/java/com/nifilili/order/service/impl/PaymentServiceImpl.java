package com.nifilili.order.service.impl;

import com.nifilili.core.enums.order.PaymentStatus;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderPaymentEntity;
import com.nifilili.order.domain.PaymentStatusHistoryEntity;
import com.nifilili.order.domain.PaymentTypeEntity;
import com.nifilili.order.dto.request.CodPaymentRequest;
import com.nifilili.order.dto.request.VerifyPaymentRequest;
import com.nifilili.order.dto.response.PaymentStatusResponse;
import com.nifilili.order.events.PaymentCompletedEvent;
import com.nifilili.order.repository.OrderPaymentRepository;
import com.nifilili.order.repository.OrderRepository;
import com.nifilili.order.repository.PaymentStatusHistoryRepository;
import com.nifilili.order.repository.PaymentTypeRepository;
import com.nifilili.order.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final OrderPaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final PaymentStatusHistoryRepository paymentHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PaymentStatusResponse getPayment(Long orderId) {
        log.debug("Getting payment for orderId={}", orderId);

        OrderPaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));

        String typeName = paymentTypeRepository.findById(payment.getPaymentTypeId())
                .map(PaymentTypeEntity::getName)
                .orElse("Unknown");

        return PaymentStatusResponse.builder()
                .paymentTypeId(payment.getPaymentTypeId())
                .typeName(typeName)
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .paymentDetails(payment.getPaymentDetails())
                .build();
    }

    @Override
    public void verifyPayment(Long orderId, VerifyPaymentRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Verifying payment for orderId={}, verified={}", orderId, request.getVerified());

        OrderPaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));

        if (payment.getStatus() != PaymentStatus.PENDING_VERIFICATION) {
            throw new InvalidOrderStateException("Payment is not in PENDING_VERIFICATION status");
        }

        PaymentStatus oldStatus = payment.getStatus();
        PaymentStatus newStatus = request.getVerified() ? PaymentStatus.VERIFIED : PaymentStatus.FAILED;

        payment.setStatus(newStatus);
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setUpdatedBy(userId);
        paymentRepository.save(payment);

        recordPaymentHistory(payment.getId(), oldStatus, newStatus, request.getReference(), userId);
        updateOrderPaymentStatus(orderId, newStatus);

        if (newStatus == PaymentStatus.VERIFIED) {
            eventPublisher.publishEvent(new PaymentCompletedEvent(orderId, payment.getId()));
        }
    }

    @Override
    public void recordCodPayment(Long orderId, CodPaymentRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Recording COD payment for orderId={}, amount={}", orderId, request.getAmountReceived());

        OrderPaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidOrderStateException("Payment is not in PENDING status");
        }

        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setUpdatedBy(userId);
        paymentRepository.save(payment);

        // Update order amount paid
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setAmountPaid(request.getAmountReceived());
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        recordPaymentHistory(payment.getId(), oldStatus, PaymentStatus.COMPLETED, null, userId);
        eventPublisher.publishEvent(new PaymentCompletedEvent(orderId, payment.getId()));
    }

    private void recordPaymentHistory(Long paymentId, PaymentStatus oldStatus,
                                       PaymentStatus newStatus, String note, Long userId) {
        PaymentStatusHistoryEntity history = PaymentStatusHistoryEntity.builder()
                .paymentId(paymentId)
                .oldStatus(oldStatus.name())
                .newStatus(newStatus.name())
                .note(note)
                .createdAt(LocalDateTime.now())
                .createdBy(userId)
                .build();
        paymentHistoryRepository.save(history);
    }

    private void updateOrderPaymentStatus(Long orderId, PaymentStatus status) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setPaymentStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}
