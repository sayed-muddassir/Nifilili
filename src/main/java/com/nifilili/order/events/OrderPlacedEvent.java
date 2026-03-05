package com.nifilili.order.events;

/**
 * Published when a new order is successfully placed.
 * Consumed by payment and notification modules.
 */
public record OrderPlacedEvent(Long orderId, Long userId, String orderNumber) {
}
