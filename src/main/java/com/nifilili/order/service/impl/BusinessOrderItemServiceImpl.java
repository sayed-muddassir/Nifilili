package com.nifilili.order.service.impl;

import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.dto.request.business.RejectOrderItemRequest;
import com.nifilili.order.dto.request.business.UpdateOrderItemStatusRequest;
import com.nifilili.order.dto.response.business.BusinessOrderItemResponse;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderStatusHistoryEntity;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderStatusHistoryRepository;
import com.nifilili.order.service.business.BusinessOrderItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BusinessOrderItemServiceImpl implements BusinessOrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    /**
     * Allowed lifecycle transitions for order items.
     * This prevents illegal jumps like placed -> delivered.
     */
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "placed",
            "received",
            "preparing",
            "shipped",
            "delivered",
            "rejected",
            "cancelled",
            "returned"
    );

    @Override
    public Page<BusinessOrderItemResponse> listItems(String status, Pageable pageable) {

        Long businessId = SecurityUtil.getCurrentUserId(); // business user context

        Page<OrderItemEntity> page;

        if (status == null) {
            page = orderItemRepository.findByBusinessId(businessId, pageable);
        } else {
            page = orderItemRepository.findByBusinessIdAndStatus(
                    businessId, status, pageable
            );
        }

        return page.map(this::mapToResponse);
    }

    /**
     * Update item status (happy path).
     * This is NOT allowed if item is already rejected/cancelled.
     */
    @Override
    @Transactional
    public void updateStatus(Long orderItemId, UpdateOrderItemStatusRequest request) {

        Long businessId = SecurityUtil.getCurrentUserId();

        OrderItemEntity item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order item not found"));

        // 🔐 Ensure business owns this item
        if (!item.getBusinessId().equals(businessId)) {
            throw new IllegalStateException("Unauthorized access to order item");
        }

        String newStatus = request.getNewStatus();

        // 🚫 Prevent invalid statuses
        if (!ALLOWED_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid order item status: " + newStatus);
        }

        // 🚫 Prevent changing terminal states
        if (isTerminalState(item.getStatus())) {
            throw new IllegalStateException(
                    "Cannot update status once item is " + item.getStatus()
            );
        }

        // 🚫 Prevent no-op updates
        if (item.getStatus().equals(newStatus)) {
            return;
        }

        // 1️⃣ Write status history
        OrderStatusHistoryEntity history = new OrderStatusHistoryEntity();
        history.setOrderItemId(item.getId());
        history.setOldStatus(item.getStatus());
        history.setNewStatus(newStatus);
        history.setCreatedAt(LocalDateTime.now());
        history.setCreatedBy(businessId);
        history.setRejectionReason(null);

        orderStatusHistoryRepository.save(history);

        // 2️⃣ Update item status
        item.setStatus(newStatus);
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(businessId);

        orderItemRepository.save(item);
    }

    /**
     * Reject an order item.
     * This is a terminal state.
     */
    @Override
    @Transactional
    public void reject(Long orderItemId, RejectOrderItemRequest request) {

        Long businessId = SecurityUtil.getCurrentUserId();

        OrderItemEntity item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order item not found"));

        // 🔐 Ownership check
        if (!item.getBusinessId().equals(businessId)) {
            throw new IllegalStateException("Unauthorized access to order item");
        }

        // 🚫 Cannot reject after delivery
        if ("delivered".equalsIgnoreCase(item.getStatus())) {
            throw new IllegalStateException("Delivered item cannot be rejected");
        }

        // 🚫 Already rejected
        if ("rejected".equalsIgnoreCase(item.getStatus())) {
            return;
        }

        // 1️⃣ Status history entry
        OrderStatusHistoryEntity history = new OrderStatusHistoryEntity();
        history.setOrderItemId(item.getId());
        history.setOldStatus(item.getStatus());
        history.setNewStatus("rejected");
        history.setRejectionReason(request.getReason());
        history.setCreatedAt(LocalDateTime.now());
        history.setCreatedBy(businessId);

        orderStatusHistoryRepository.save(history);

        // 2️⃣ Update item
        item.setStatus("rejected");
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(businessId);

        orderItemRepository.save(item);
    }

    // ------------------------
    // Helper methods
    // ------------------------

    private boolean isTerminalState(String status) {
        return Set.of("rejected", "cancelled", "returned").contains(status);
    }

    private BusinessOrderItemResponse mapToResponse(OrderItemEntity entity) {

        BusinessOrderItemResponse response = new BusinessOrderItemResponse();
        response.setOrderItemId(entity.getId());
        response.setOrderId(entity.getOrderId());
        response.setTitle(entity.getTitle());
        response.setQuantity(entity.getQuantity());
        response.setSubtotal(entity.getSubtotal());
        response.setStatus(entity.getStatus());

        return response;
    }
}
