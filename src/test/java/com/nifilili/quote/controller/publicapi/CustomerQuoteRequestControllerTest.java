package com.nifilili.quote.controller.publicapi;

import com.nifilili.quote.dto.request.CreateQuoteRequestRequest;
import com.nifilili.quote.dto.request.RejectQuoteRequest;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerQuoteRequestControllerTest {

    private static final Long REQUEST_ID = 1L;
    private static final Long QUOTE_ID = 10L;

    @Mock
    private QuoteRequestService quoteRequestService;

    @Mock
    private QuoteService quoteService;

    @InjectMocks
    private CustomerQuoteRequestController controller;

    @Test
    void createRequest_ShouldReturn201() {
        CreateQuoteRequestRequest request = new CreateQuoteRequestRequest();
        request.setOfferingId(100L);
        request.setBusinessId(200L);
        request.setRequirements("Build website");

        QuoteRequestResponse response = QuoteRequestResponse.builder().requestId(REQUEST_ID).build();
        when(quoteRequestService.createRequest(any())).thenReturn(response);

        ResponseEntity<QuoteRequestResponse> result = controller.createRequest(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getRequestId()).isEqualTo(REQUEST_ID);
        verify(quoteRequestService).createRequest(request);
    }

    @Test
    void listMyRequests_ShouldReturn200WithList() {
        QuoteRequestResponse response = QuoteRequestResponse.builder().requestId(REQUEST_ID).build();
        when(quoteRequestService.listMyRequests()).thenReturn(List.of(response));

        ResponseEntity<List<QuoteRequestResponse>> result = controller.listMyRequests();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
        verify(quoteRequestService).listMyRequests();
    }

    @Test
    void getRequest_ShouldReturn200() {
        QuoteRequestResponse response = QuoteRequestResponse.builder().requestId(REQUEST_ID).build();
        when(quoteRequestService.getRequest(REQUEST_ID)).thenReturn(response);

        ResponseEntity<QuoteRequestResponse> result = controller.getRequest(REQUEST_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getRequestId()).isEqualTo(REQUEST_ID);
        verify(quoteRequestService).getRequest(REQUEST_ID);
    }

    @Test
    void cancelRequest_ShouldReturn200() {
        QuoteRequestResponse response = QuoteRequestResponse.builder()
                .requestId(REQUEST_ID).status("CANCELLED").build();
        when(quoteRequestService.cancelRequest(REQUEST_ID)).thenReturn(response);

        ResponseEntity<QuoteRequestResponse> result = controller.cancelRequest(REQUEST_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getStatus()).isEqualTo("CANCELLED");
        verify(quoteRequestService).cancelRequest(REQUEST_ID);
    }

    @Test
    void listQuotes_ShouldReturn200WithList() {
        QuoteResponse quote = QuoteResponse.builder().quoteId(QUOTE_ID).build();
        when(quoteService.listQuotesForRequest(REQUEST_ID)).thenReturn(List.of(quote));

        ResponseEntity<List<QuoteResponse>> result = controller.listQuotes(REQUEST_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
        verify(quoteService).listQuotesForRequest(REQUEST_ID);
    }

    @Test
    void getLatestQuote_ShouldReturn200() {
        QuoteResponse quote = QuoteResponse.builder().quoteId(QUOTE_ID).build();
        when(quoteService.getLatestQuote(REQUEST_ID)).thenReturn(quote);

        ResponseEntity<QuoteResponse> result = controller.getLatestQuote(REQUEST_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getQuoteId()).isEqualTo(QUOTE_ID);
        verify(quoteService).getLatestQuote(REQUEST_ID);
    }

    @Test
    void acceptQuote_ShouldReturn200() {
        QuoteResponse quote = QuoteResponse.builder().quoteId(QUOTE_ID).status("ACCEPTED").build();
        when(quoteService.acceptQuote(QUOTE_ID)).thenReturn(quote);

        ResponseEntity<QuoteResponse> result = controller.acceptQuote(QUOTE_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getStatus()).isEqualTo("ACCEPTED");
        verify(quoteService).acceptQuote(QUOTE_ID);
    }

    @Test
    void rejectQuote_ShouldReturn200() {
        RejectQuoteRequest rejectRequest = new RejectQuoteRequest();
        rejectRequest.setReason("Too expensive");
        QuoteResponse quote = QuoteResponse.builder().quoteId(QUOTE_ID).status("REJECTED").build();
        when(quoteService.rejectQuote(eq(QUOTE_ID), any())).thenReturn(quote);

        ResponseEntity<QuoteResponse> result = controller.rejectQuote(QUOTE_ID, rejectRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getStatus()).isEqualTo("REJECTED");
        verify(quoteService).rejectQuote(QUOTE_ID, rejectRequest);
    }
}
