package com.nifilili.order.events;

/**
 * Published when a refund has been processed.
 * Consumed by notification module.
 */
public record RefundProcessedEvent(Long orderId, Long refundId) {
}
