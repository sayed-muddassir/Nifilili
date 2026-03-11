package com.nifilili.quote.controller.publicapi;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.quote.dto.request.CreateQuoteRequestRequest;
import com.nifilili.quote.dto.request.RejectQuoteRequest;
import com.nifilili.quote.dto.response.QuoteRequestResponse;
import com.nifilili.quote.dto.response.QuoteResponse;
import com.nifilili.quote.service.QuoteRequestService;
import com.nifilili.quote.service.QuoteService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/quote-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = SwaggerConstants.QUOTE_1, description = "Customer quote request and quote decision endpoints")
@Hidden
public class CustomerQuoteRequestController {

    private final QuoteRequestService quoteRequestService;
    private final QuoteService quoteService;

    @PostMapping
    @Operation(summary = "Submit quote request",
            description = "Creates a new quote request for a service offering with dynamic pricing")
    @ApiResponse(responseCode = "201", description = "Quote request created")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<QuoteRequestResponse> createRequest(
            @Valid @RequestBody CreateQuoteRequestRequest request) {
        log.info("POST /api/v1/quote-requests - offeringId={}", request.getOfferingId());
        return ResponseEntity.status(HttpStatus.CREATED).body(quoteRequestService.createRequest(request));
    }

    @GetMapping("/mine")
    @Operation(summary = "List my quote requests",
            description = "Returns all quote requests submitted by the authenticated user")
    @ApiResponse(responseCode = "200", description = "Quote requests returned")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<List<QuoteRequestResponse>> listMyRequests() {
        log.info("GET /api/v1/quote-requests/mine");
        return ResponseEntity.ok(quoteRequestService.listMyRequests());
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "Get quote request details",
            description = "Returns details of a specific quote request")
    @ApiResponse(responseCode = "200", description = "Quote request returned")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "404", description = "Quote request not found")
    public ResponseEntity<QuoteRequestResponse> getRequest(@PathVariable Long requestId) {
        log.info("GET /api/v1/quote-requests/{}", requestId);
        return ResponseEntity.ok(quoteRequestService.getRequest(requestId));
    }

    @PostMapping("/{requestId}/cancel")
    @Operation(summary = "Cancel quote request",
            description = "Cancels a pending quote request owned by the authenticated user")
    @ApiResponse(responseCode = "200", description = "Quote request cancelled")
    @ApiResponse(responseCode = "400", description = "Request not in cancellable state")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "404", description = "Quote request not found")
    public ResponseEntity<QuoteRequestResponse> cancelRequest(@PathVariable Long requestId) {
        log.info("POST /api/v1/quote-requests/{}/cancel", requestId);
        return ResponseEntity.ok(quoteRequestService.cancelRequest(requestId));
    }

    @GetMapping("/{requestId}/quotes")
    @Operation(summary = "List quotes for request",
            description = "Returns all quote versions for a given quote request")
    @ApiResponse(responseCode = "200", description = "Quotes returned")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<List<QuoteResponse>> listQuotes(@PathVariable Long requestId) {
        log.info("GET /api/v1/quote-requests/{}/quotes", requestId);
        return ResponseEntity.ok(quoteService.listQuotesForRequest(requestId));
    }

    @GetMapping("/{requestId}/quotes/latest")
    @Operation(summary = "Get latest quote",
            description = "Returns the most recent quote for a given quote request")
    @ApiResponse(responseCode = "200", description = "Latest quote returned")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "404", description = "No quotes found")
    public ResponseEntity<QuoteResponse> getLatestQuote(@PathVariable Long requestId) {
        log.info("GET /api/v1/quote-requests/{}/quotes/latest", requestId);
        return ResponseEntity.ok(quoteService.getLatestQuote(requestId));
    }

    @PostMapping("/quotes/{quoteId}/accept")
    @Operation(summary = "Accept quote",
            description = "Accepts a sent quote, triggering order creation")
    @ApiResponse(responseCode = "200", description = "Quote accepted")
    @ApiResponse(responseCode = "400", description = "Quote not in acceptable state or expired")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "404", description = "Quote not found")
    public ResponseEntity<QuoteResponse> acceptQuote(@PathVariable Long quoteId) {
        log.info("POST /api/v1/quote-requests/quotes/{}/accept", quoteId);
        return ResponseEntity.ok(quoteService.acceptQuote(quoteId));
    }

    @PostMapping("/quotes/{quoteId}/reject")
    @Operation(summary = "Reject quote",
            description = "Rejects a sent quote with an optional reason")
    @ApiResponse(responseCode = "200", description = "Quote rejected")
    @ApiResponse(responseCode = "400", description = "Quote not in rejectable state")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "404", description = "Quote not found")
    public ResponseEntity<QuoteResponse> rejectQuote(
            @PathVariable Long quoteId,
            @RequestBody(required = false) RejectQuoteRequest request) {
        log.info("POST /api/v1/quote-requests/quotes/{}/reject", quoteId);
        return ResponseEntity.ok(quoteService.rejectQuote(quoteId, request));
    }
}
