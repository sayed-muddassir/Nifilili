package com.nifilili.order.events;

import java.math.BigDecimal;

/**
 * Published when a cancellation request for a paid item is approved.
 * Signals that a refund may be required.
 */
public record CancellationApprovedEvent(Long orderId, Long orderItemId, BigDecimal refundableAmount) {
}
