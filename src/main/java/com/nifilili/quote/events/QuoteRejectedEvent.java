package com.nifilili.quote.events;

/**
 * Published when a customer rejects a quote.
 * Consumed by notification module to alert the business.
 */
public record QuoteRejectedEvent(
        Long quoteId,
        Long requestId,
        Long userId,
        Long businessId,
        String reason
) {
}
