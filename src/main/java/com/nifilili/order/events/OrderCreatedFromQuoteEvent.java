package com.nifilili.order.events;

/**
 * Published when an order is created from an accepted quote.
 * Consumed by quote module to record the quote-to-order conversion.
 */
public record OrderCreatedFromQuoteEvent(
        Long quoteId,
        Long orderId,
        String orderNumber,
        Long convertedBy
) {
}
