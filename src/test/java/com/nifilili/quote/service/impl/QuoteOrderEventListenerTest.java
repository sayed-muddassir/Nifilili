package com.nifilili.quote.service.impl;

import com.nifilili.order.events.OrderCreatedFromQuoteEvent;
import com.nifilili.quote.service.QuoteConversionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuoteOrderEventListenerTest {

    private static final Long QUOTE_ID = 10L;
    private static final Long ORDER_ID = 20L;
    private static final Long USER_ID = 42L;

    @Mock
    private QuoteConversionService quoteConversionService;

    @InjectMocks
    private QuoteOrderEventListener quoteOrderEventListener;

    @Test
    void onOrderCreatedFromQuote_ShouldRecordConversion() {
        OrderCreatedFromQuoteEvent event = new OrderCreatedFromQuoteEvent(
                QUOTE_ID, ORDER_ID, "ORD-TEST1234", USER_ID);

        quoteOrderEventListener.onOrderCreatedFromQuote(event);

        verify(quoteConversionService).recordConversion(QUOTE_ID, ORDER_ID, USER_ID);
    }
}
