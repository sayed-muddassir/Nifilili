package com.nifilili.quote.events;

/**
 * Published when a customer submits a new quote request.
 * Consumed by notification module to alert the business.
 */
public record QuoteRequestCreatedEvent(
        Long requestId,
        Long offeringId,
        Long userId,
        Long businessId,
        String requestNumber
) {
}
