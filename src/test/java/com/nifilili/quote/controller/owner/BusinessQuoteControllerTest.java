package com.nifilili.quote.controller.owner;

import com.nifilili.quote.dto.request.CreateQuoteRequest;
import com.nifilili.quote.dto.request.QuoteLineItemRequest;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessQuoteControllerTest {

    private static final Long BUSINESS_ID = 100L;
    private static final Long REQUEST_ID = 1L;
    private static final Long QUOTE_ID = 10L;

    @Mock
    private QuoteRequestService quoteRequestService;

    @Mock
    private QuoteService quoteService;

    @InjectMocks
    private BusinessQuoteController controller;

    @Test
    void listInbox_ShouldReturn200WithList() {
        QuoteRequestResponse response = QuoteRequestResponse.builder().requestId(REQUEST_ID).build();
        when(quoteRequestService.listBusinessRequests(BUSINESS_ID)).thenReturn(List.of(response));

        ResponseEntity<List<QuoteRequestResponse>> result = controller.listInbox(BUSINESS_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
        verify(quoteRequestService).listBusinessRequests(BUSINESS_ID);
    }

    @Test
    void getRequestDetails_ShouldReturn200() {
        QuoteRequestResponse response = QuoteRequestResponse.builder().requestId(REQUEST_ID).build();
        when(quoteRequestService.getRequest(REQUEST_ID)).thenReturn(response);

        ResponseEntity<QuoteRequestResponse> result = controller.getRequestDetails(REQUEST_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getRequestId()).isEqualTo(REQUEST_ID);
        verify(quoteRequestService).getRequest(REQUEST_ID);
    }

    @Test
    void declineRequest_ShouldReturn200() {
        QuoteRequestResponse response = QuoteRequestResponse.builder()
                .requestId(REQUEST_ID).status("REJECTED").build();
        when(quoteRequestService.declineRequest(REQUEST_ID, "Not feasible")).thenReturn(response);

        ResponseEntity<QuoteRequestResponse> result = controller.declineRequest(REQUEST_ID, "Not feasible");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getStatus()).isEqualTo("REJECTED");
        verify(quoteRequestService).declineRequest(REQUEST_ID, "Not feasible");
    }

    @Test
    void createDraft_ShouldReturn201() {
        CreateQuoteRequest request = buildCreateQuoteRequest();
        QuoteResponse response = QuoteResponse.builder().quoteId(QUOTE_ID).status("DRAFT").build();
        when(quoteService.createDraft(eq(REQUEST_ID), any())).thenReturn(response);

        ResponseEntity<QuoteResponse> result = controller.createDraft(REQUEST_ID, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody().getQuoteId()).isEqualTo(QUOTE_ID);
        assertThat(result.getBody().getStatus()).isEqualTo("DRAFT");
        verify(quoteService).createDraft(REQUEST_ID, request);
    }

    @Test
    void sendQuote_ShouldReturn200() {
        QuoteResponse response = QuoteResponse.builder().quoteId(QUOTE_ID).status("SENT").build();
        when(quoteService.sendQuote(QUOTE_ID)).thenReturn(response);

        ResponseEntity<QuoteResponse> result = controller.sendQuote(QUOTE_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getStatus()).isEqualTo("SENT");
        verify(quoteService).sendQuote(QUOTE_ID);
    }

    @Test
    void reviseQuote_ShouldReturn201() {
        CreateQuoteRequest request = buildCreateQuoteRequest();
        QuoteResponse response = QuoteResponse.builder().quoteId(QUOTE_ID + 1).status("DRAFT").build();
        when(quoteService.reviseQuote(eq(QUOTE_ID), any())).thenReturn(response);

        ResponseEntity<QuoteResponse> result = controller.reviseQuote(QUOTE_ID, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody().getQuoteId()).isEqualTo(QUOTE_ID + 1);
        verify(quoteService).reviseQuote(QUOTE_ID, request);
    }

    @Test
    void getQuote_ShouldReturn200() {
        QuoteResponse response = QuoteResponse.builder().quoteId(QUOTE_ID).build();
        when(quoteService.getQuote(QUOTE_ID)).thenReturn(response);

        ResponseEntity<QuoteResponse> result = controller.getQuote(QUOTE_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getQuoteId()).isEqualTo(QUOTE_ID);
        verify(quoteService).getQuote(QUOTE_ID);
    }

    private CreateQuoteRequest buildCreateQuoteRequest() {
        QuoteLineItemRequest lineItem = new QuoteLineItemRequest();
        lineItem.setDescription("Design phase");
        lineItem.setQuantity(1);
        lineItem.setUnitPrice(new BigDecimal("500.00"));

        CreateQuoteRequest request = new CreateQuoteRequest();
        request.setServiceDetails("Website development");
        request.setCurrency("NPR");
        request.setValidityDays(14);
        request.setLineItems(List.of(lineItem));
        return request;
    }
}
