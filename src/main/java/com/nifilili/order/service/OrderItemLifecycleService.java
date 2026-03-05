package com.nifilili.order.service;

import com.nifilili.order.dto.request.RejectOrderItemRequest;
import com.nifilili.order.dto.request.UpdateOrderItemStatusRequest;

public interface OrderItemLifecycleService {

    /**
     * Updates the status of an order item, records status history, and re-derives the
     * overall order status.
     *
     * @param orderItemId the order item ID
     * @param request     the new status and optional note
     * @throws com.nifilili.core.exception.ResourceNotFoundException  if order item not found
     * @throws com.nifilili.core.exception.InvalidOrderStateException if the status transition is not allowed
     */
    void updateStatus(Long orderItemId, UpdateOrderItemStatusRequest request);

    /**
     * Rejects an order item with a mandatory reason, records status history, and re-derives
     * the overall order status.
     *
     * @param orderItemId the order item ID
     * @param request     the rejection reason
     * @throws com.nifilili.core.exception.ResourceNotFoundException  if order item not found
     * @throws com.nifilili.core.exception.InvalidOrderStateException if the item cannot be rejected
     */
    void reject(Long orderItemId, RejectOrderItemRequest request);
}
