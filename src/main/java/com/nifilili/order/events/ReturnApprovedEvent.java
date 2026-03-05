package com.nifilili.order.events;

/**
 * Published when a return request is approved after inspection.
 * Signals that a refund should be initiated.
 */
public record ReturnApprovedEvent(Long orderId, Long orderItemId, Long returnRequestId) {
}
