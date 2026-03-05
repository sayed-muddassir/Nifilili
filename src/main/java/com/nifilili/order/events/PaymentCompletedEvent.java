package com.nifilili.order.events;

/**
 * Published when a payment is marked as completed (verified or COD collected).
 * Consumed by notification and fulfillment modules.
 */
public record PaymentCompletedEvent(Long orderId, Long paymentId) {
}
