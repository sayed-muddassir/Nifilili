package com.nifilili.quote.events;

/**
 * Published when a quote or quote request expires.
 * Consumed by notification module for audit and alerting.
 */
public record QuoteExpiredEvent(
        Long quoteId,
        Long requestId,
        Long userId,
        Long businessId
) {
}
