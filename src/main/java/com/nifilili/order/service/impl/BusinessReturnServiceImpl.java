package com.nifilili.order.service.impl;

import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.ReturnStatus;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.ReturnRequestEntity;
import com.nifilili.order.dto.request.UpdateReturnStatusRequest;
import com.nifilili.order.dto.response.ReturnResponse;
import com.nifilili.order.events.ReturnApprovedEvent;
import com.nifilili.order.mapper.ReturnMapper;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.ReturnRequestRepository;
import com.nifilili.order.service.BusinessReturnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BusinessReturnServiceImpl implements BusinessReturnService {

    private static final Map<ReturnStatus, Set<ReturnStatus>> ALLOWED_TRANSITIONS = Map.of(
            ReturnStatus.REQUESTED, Set.of(ReturnStatus.PICKUP_SCHEDULED, ReturnStatus.REJECTED),
            ReturnStatus.PICKUP_SCHEDULED, Set.of(ReturnStatus.PICKED_UP),
            ReturnStatus.PICKED_UP, Set.of(ReturnStatus.RECEIVED),
            ReturnStatus.RECEIVED, Set.of(ReturnStatus.INSPECTED),
            ReturnStatus.INSPECTED, Set.of(ReturnStatus.REFUNDED, ReturnStatus.REJECTED)
    );

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReturnMapper returnMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ReturnResponse updateStatus(Long returnRequestId, UpdateReturnStatusRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Updating return status: returnRequestId={}, newStatus={}", returnRequestId, request.getStatus());
        LocalDateTime now = LocalDateTime.now();

        ReturnRequestEntity returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found"));

        ReturnStatus newStatus = ReturnStatus.valueOf(request.getStatus());
        ReturnStatus oldStatus = returnRequest.getStatus();

        validateTransition(oldStatus, newStatus);

        if (newStatus == ReturnStatus.REJECTED && (request.getRejectionReason() == null
                || request.getRejectionReason().isBlank())) {
            throw new InvalidOrderStateException("Rejection reason is required");
        }

        returnRequest.setStatus(newStatus);
        returnRequest.setUpdatedAt(now);
        returnRequest.setUpdatedBy(userId);

        // Set lifecycle timestamps
        switch (newStatus) {
            case PICKUP_SCHEDULED -> returnRequest.setPickupScheduledAt(now);
            case RECEIVED -> returnRequest.setReceivedAt(now);
            case INSPECTED -> returnRequest.setInspectedAt(now);
            case REJECTED -> {
                returnRequest.setRejectedAt(now);
                returnRequest.setRejectionReason(request.getRejectionReason());
            }
            case REFUNDED -> {
                // Update order item status to RETURNED
                OrderItemEntity item = orderItemRepository.findById(returnRequest.getOrderItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
                item.setStatus(OrderItemStatus.RETURNED);
                item.setUpdatedAt(now);
                item.setUpdatedBy(userId);
                orderItemRepository.save(item);

                eventPublisher.publishEvent(new ReturnApprovedEvent(
                        item.getOrderId(), item.getId(), returnRequest.getId()));
            }
            default -> { /* no additional action */ }
        }

        returnRequest = returnRequestRepository.save(returnRequest);
        return returnMapper.toResponse(returnRequest);
    }

    private void validateTransition(ReturnStatus from, ReturnStatus to) {
        Set<ReturnStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new InvalidOrderStateException(
                    "Cannot transition return from " + from + " to " + to);
        }
    }
}
