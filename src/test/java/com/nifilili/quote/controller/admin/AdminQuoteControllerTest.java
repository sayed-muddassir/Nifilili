package com.nifilili.quote.controller.admin;

import com.nifilili.quote.dto.response.QuoteRequestResponse;
import com.nifilili.quote.dto.response.QuoteResponse;
import com.nifilili.quote.service.QuoteRequestService;
import com.nifilili.quote.service.QuoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminQuoteControllerTest {

    private static final Long REQUEST_ID = 1L;
    private static final Long QUOTE_ID = 10L;

    @Mock
    private QuoteRequestService quoteRequestService;

    @Mock
    private QuoteService quoteService;

    @InjectMocks
    private AdminQuoteController controller;

    @Test
    void getRequest_ShouldReturn200() {
        QuoteRequestResponse response = QuoteRequestResponse.builder().requestId(REQUEST_ID).build();
        when(quoteRequestService.getRequest(REQUEST_ID)).thenReturn(response);

        ResponseEntity<QuoteRequestResponse> result = controller.getRequest(REQUEST_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getRequestId()).isEqualTo(REQUEST_ID);
        verify(quoteRequestService).getRequest(REQUEST_ID);
    }

    @Test
    void getQuote_ShouldReturn200() {
        QuoteResponse response = QuoteResponse.builder().quoteId(QUOTE_ID).build();
        when(quoteService.getQuote(QUOTE_ID)).thenReturn(response);

        ResponseEntity<QuoteResponse> result = controller.getQuote(QUOTE_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getQuoteId()).isEqualTo(QUOTE_ID);
        verify(quoteService).getQuote(QUOTE_ID);
    }
}
