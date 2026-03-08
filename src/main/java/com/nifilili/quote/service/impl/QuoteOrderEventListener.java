package com.nifilili.quote.service.impl;

import com.nifilili.order.events.OrderCreatedFromQuoteEvent;
import com.nifilili.quote.service.QuoteConversionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for order creation events triggered by quote acceptance.
 * Records the quote-to-order conversion when the order module confirms
 * successful order creation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteOrderEventListener {

    private final QuoteConversionService quoteConversionService;

    @EventListener
    public void onOrderCreatedFromQuote(OrderCreatedFromQuoteEvent event) {
        log.info("Order created from quote: quoteId={}, orderId={}, orderNumber={}",
                event.quoteId(), event.orderId(), event.orderNumber());
        quoteConversionService.recordConversion(event.quoteId(), event.orderId(), event.convertedBy());
    }
}
