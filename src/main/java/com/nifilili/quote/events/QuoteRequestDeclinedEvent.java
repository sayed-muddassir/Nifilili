package com.nifilili.quote.events;

/**
 * Published when a business declines a quote request.
 * Consumed by notification module to alert the customer.
 */
public record QuoteRequestDeclinedEvent(
        Long requestId,
        Long userId,
        Long businessId,
        String reason
) {
}
