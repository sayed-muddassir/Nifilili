package com.nifilili.quote.events;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Published when a customer accepts a quote.
 * Consumed by order module to create an order from the accepted quote.
 * Carries all data needed for order creation so the order module has no
 * dependency on the quote module's repositories.
 */
public record QuoteAcceptedEvent(
        Long quoteId,
        Long requestId,
        Long userId,
        Long businessId,
        Long offeringId,
        String quoteNumber,
        String serviceDetails,
        BigDecimal totalAmount,
        String currency,
        Map<String, Object> deliveryAddress,
        List<LineItemData> lineItems
) {

    public record LineItemData(
            String description,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {
    }
}
