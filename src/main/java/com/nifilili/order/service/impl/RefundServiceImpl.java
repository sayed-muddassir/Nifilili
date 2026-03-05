package com.nifilili.order.service.impl;

import com.nifilili.core.enums.order.RefundStatus;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.domain.OrderRefundEntity;
import com.nifilili.order.dto.request.CreateRefundRequest;
import com.nifilili.order.dto.request.UpdateRefundStatusRequest;
import com.nifilili.order.dto.response.RefundResponse;
import com.nifilili.order.events.RefundProcessedEvent;
import com.nifilili.order.mapper.RefundMapper;
import com.nifilili.order.repository.OrderRefundRepository;
import com.nifilili.order.repository.OrderRepository;
import com.nifilili.order.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefundServiceImpl implements RefundService {

    private static final Map<RefundStatus, Set<RefundStatus>> ALLOWED_TRANSITIONS = Map.of(
            RefundStatus.PENDING, Set.of(RefundStatus.APPROVED, RefundStatus.CANCELLED),
            RefundStatus.APPROVED, Set.of(RefundStatus.PROCESSED, RefundStatus.FAILED)
    );

    private final OrderRefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final RefundMapper refundMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public RefundResponse createRefund(Long orderId, CreateRefundRequest request) {
        log.info("Creating refund for orderId={}", orderId);

        orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check for existing refund
        if (refundRepository.findByOrderId(orderId).isPresent()) {
            throw new InvalidOrderStateException("Refund already exists for this order");
        }

        OrderRefundEntity refund = OrderRefundEntity.builder()
                .orderId(orderId)
                .refundAmount(BigDecimal.ZERO) // Will be calculated based on cancelled/returned items
                .status(RefundStatus.PENDING)
                .bankName(request.getBankName())
                .bankAccountName(request.getAccountHolder())
                .accountNumber(request.getAccountNumber())
                .branch(request.getBranch())
                .build();

        refund = refundRepository.save(refund);
        return refundMapper.toResponse(refund);
    }

    @Override
    public RefundResponse updateRefundStatus(Long refundId, UpdateRefundStatusRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Updating refund status: refundId={}, newStatus={}", refundId, request.getStatus());

        OrderRefundEntity refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found"));

        RefundStatus newStatus = RefundStatus.valueOf(request.getStatus());
        RefundStatus oldStatus = refund.getStatus();

        validateTransition(oldStatus, newStatus);

        refund.setStatus(newStatus);
        if (request.getReference() != null) {
            refund.setRefundReference(request.getReference());
        }

        if (newStatus == RefundStatus.PROCESSED) {
            refund.setProcessedAt(LocalDateTime.now());
            eventPublisher.publishEvent(new RefundProcessedEvent(refund.getOrderId(), refund.getId()));
        }

        refund = refundRepository.save(refund);
        return refundMapper.toResponse(refund);
    }

    private void validateTransition(RefundStatus from, RefundStatus to) {
        Set<RefundStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new InvalidOrderStateException(
                    "Cannot transition refund from " + from + " to " + to);
        }
    }
}
