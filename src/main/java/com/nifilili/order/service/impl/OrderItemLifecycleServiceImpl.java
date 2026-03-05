package com.nifilili.order.service.impl;

import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.OrderStatus;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderStatusHistoryEntity;
import com.nifilili.order.dto.request.RejectOrderItemRequest;
import com.nifilili.order.dto.request.UpdateOrderItemStatusRequest;
import com.nifilili.order.events.OrderItemStatusChangedEvent;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderRepository;
import com.nifilili.order.repository.OrderStatusHistoryRepository;
import com.nifilili.order.service.OrderItemLifecycleService;
import com.nifilili.order.util.OrderStatusDeriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemLifecycleServiceImpl implements OrderItemLifecycleService {

    private static final Map<OrderItemStatus, Set<OrderItemStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderItemStatus.PLACED, Set.of(OrderItemStatus.RECEIVED, OrderItemStatus.CANCELLED, OrderItemStatus.REJECTED),
            OrderItemStatus.RECEIVED, Set.of(OrderItemStatus.PREPARING, OrderItemStatus.CANCELLED, OrderItemStatus.REJECTED),
            OrderItemStatus.PREPARING, Set.of(OrderItemStatus.SHIPPED),
            OrderItemStatus.SHIPPED, Set.of(OrderItemStatus.DELIVERED),
            OrderItemStatus.DELIVERED, Set.of(OrderItemStatus.RETURNED)
    );

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void updateStatus(Long orderItemId, UpdateOrderItemStatusRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Updating order item status: orderItemId={}, newStatus={}", orderItemId, request.getNewStatus());

        OrderItemEntity item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        OrderItemStatus newStatus = OrderItemStatus.valueOf(request.getNewStatus());
        OrderItemStatus oldStatus = item.getStatus();

        validateTransition(oldStatus, newStatus);

        item.setStatus(newStatus);
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(userId);
        orderItemRepository.save(item);

        recordHistory(item.getId(), oldStatus.name(), newStatus.name(), request.getNote(), userId);
        rederiveOrderStatus(item.getOrderId());

        eventPublisher.publishEvent(new OrderItemStatusChangedEvent(
                item.getOrderId(), item.getId(), oldStatus, newStatus));
    }

    @Override
    public void reject(Long orderItemId, RejectOrderItemRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Rejecting order item: orderItemId={}", orderItemId);

        OrderItemEntity item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        OrderItemStatus oldStatus = item.getStatus();
        if (oldStatus != OrderItemStatus.PLACED && oldStatus != OrderItemStatus.RECEIVED) {
            throw new InvalidOrderStateException(
                    "Cannot reject item in status: " + oldStatus);
        }

        item.setStatus(OrderItemStatus.REJECTED);
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(userId);
        orderItemRepository.save(item);

        recordHistory(item.getId(), oldStatus.name(), OrderItemStatus.REJECTED.name(), request.getReason(), userId);
        rederiveOrderStatus(item.getOrderId());

        eventPublisher.publishEvent(new OrderItemStatusChangedEvent(
                item.getOrderId(), item.getId(), oldStatus, OrderItemStatus.REJECTED));
    }

    private void validateTransition(OrderItemStatus from, OrderItemStatus to) {
        Set<OrderItemStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new InvalidOrderStateException(
                    "Cannot transition from " + from + " to " + to);
        }
    }

    private void recordHistory(Long orderItemId, String oldStatus, String newStatus, String note, Long userId) {
        OrderStatusHistoryEntity history = OrderStatusHistoryEntity.builder()
                .orderItemId(orderItemId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .rejectionReason(note)
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);
    }

    private void rederiveOrderStatus(Long orderId) {
        List<OrderItemEntity> items = orderItemRepository.findByOrderId(orderId);
        OrderStatus derivedStatus = OrderStatusDeriver.derive(items);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != derivedStatus) {
            log.info("Order status changed: orderId={}, {} -> {}", orderId, order.getStatus(), derivedStatus);
            order.setStatus(derivedStatus);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
    }
}
