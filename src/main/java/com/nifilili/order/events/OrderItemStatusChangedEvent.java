package com.nifilili.order.events;

import com.nifilili.core.enums.order.OrderItemStatus;

/**
 * Published when an order item transitions to a new status.
 * Consumed by notification and analytics modules.
 */
public record OrderItemStatusChangedEvent(
        Long orderId,
        Long orderItemId,
        OrderItemStatus oldStatus,
        OrderItemStatus newStatus
) {
}
