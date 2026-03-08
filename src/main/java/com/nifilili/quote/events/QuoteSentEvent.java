package com.nifilili.quote.events;

import java.math.BigDecimal;

/**
 * Published when a business sends a quote to the customer.
 * Consumed by notification module to alert the customer.
 */
public record QuoteSentEvent(
        Long quoteId,
        Long requestId,
        Long userId,
        Long businessId,
        String quoteNumber,
        BigDecimal totalAmount
) {
}
