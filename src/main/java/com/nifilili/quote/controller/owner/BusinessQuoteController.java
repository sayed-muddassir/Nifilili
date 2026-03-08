package com.nifilili.quote.controller.owner;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.quote.dto.request.CreateQuoteRequest;
import com.nifilili.quote.dto.response.QuoteRequestResponse;
import com.nifilili.quote.dto.response.QuoteResponse;
import com.nifilili.quote.service.QuoteRequestService;
import com.nifilili.quote.service.QuoteService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/business/quotes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = SwaggerConstants.QUOTE_2, description = "Business quote management endpoints for creating and managing quotes")
public class BusinessQuoteController {

    private final QuoteRequestService quoteRequestService;
    private final QuoteService quoteService;

    @GetMapping("/inbox")
    @Operation(summary = "List quote request inbox",
            description = "Returns all quote requests received by the specified business")
    @ApiResponse(responseCode = "200", description = "Quote requests returned")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<List<QuoteRequestResponse>> listInbox(@RequestParam Long businessId) {
        log.info("GET /api/v1/business/quotes/inbox - businessId={}", businessId);
        return ResponseEntity.ok(quoteRequestService.listBusinessRequests(businessId));
    }

    @GetMapping("/requests/{requestId}")
    @Operation(summary = "Get quote request details",
            description = "Returns details of a specific quote request for business review")
    @ApiResponse(responseCode = "200", description = "Quote request returned")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "404", description = "Quote request not found")
    public ResponseEntity<QuoteRequestResponse> getRequestDetails(@PathVariable Long requestId) {
        log.info("GET /api/v1/business/quotes/requests/{}", requestId);
        return ResponseEntity.ok(quoteRequestService.getRequest(requestId));
    }

    @PostMapping("/requests/{requestId}/decline")
    @Operation(summary = "Decline quote request",
            description = "Declines a pending quote request with a reason")
    @ApiResponse(responseCode = "200", description = "Quote request declined")
    @ApiResponse(responseCode = "400", description = "Request not in declinable state")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "404", description = "Quote request not found")
    public ResponseEntity<QuoteRequestResponse> declineRequest(
            @PathVariable Long requestId,
            @RequestParam String reason) {
        log.info("POST /api/v1/business/quotes/requests/{}/decline", requestId);
        return ResponseEntity.ok(quoteRequestService.declineRequest(requestId, reason));
    }

    @PostMapping("/requests/{requestId}/draft")
    @Operation(summary = "Create draft quote",
            description = "Creates a draft quote for a quote request with line items and pricing")
    @ApiResponse(responseCode = "201", description = "Draft quote created")
    @ApiResponse(responseCode = "400", description = "Invalid request or request in terminal state")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "404", description = "Quote request not found")
    public ResponseEntity<QuoteResponse> createDraft(
            @PathVariable Long requestId,
            @Valid @RequestBody CreateQuoteRequest request) {
        log.info("POST /api/v1/business/quotes/requests/{}/draft", requestId);
        return ResponseEntity.status(HttpStatus.CREATED).body(quoteService.createDraft(requestId, request));
    }

    @PostMapping("/{quoteId}/send")
    @Operation(summary = "Send quote to customer",
            description = "Sends a draft quote to the customer, making it visible and actionable")
    @ApiResponse(responseCode = "200", description = "Quote sent")
    @ApiResponse(responseCode = "400", description = "Quote not in draft state")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "404", description = "Quote not found")
    public ResponseEntity<QuoteResponse> sendQuote(@PathVariable Long quoteId) {
        log.info("POST /api/v1/business/quotes/{}/send", quoteId);
        return ResponseEntity.ok(quoteService.sendQuote(quoteId));
    }

    @PostMapping("/{quoteId}/revise")
    @Operation(summary = "Revise quote",
            description = "Creates a new revision of a sent quote, marking the old one as revised")
    @ApiResponse(responseCode = "201", description = "Revised quote draft created")
    @ApiResponse(responseCode = "400", description = "Quote not in sent state")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "404", description = "Quote not found")
    public ResponseEntity<QuoteResponse> reviseQuote(
            @PathVariable Long quoteId,
            @Valid @RequestBody CreateQuoteRequest request) {
        log.info("POST /api/v1/business/quotes/{}/revise", quoteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(quoteService.reviseQuote(quoteId, request));
    }

    @GetMapping("/{quoteId}")
    @Operation(summary = "Get quote details",
            description = "Returns details of a specific quote with its line items")
    @ApiResponse(responseCode = "200", description = "Quote returned")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "404", description = "Quote not found")
    public ResponseEntity<QuoteResponse> getQuote(@PathVariable Long quoteId) {
        log.info("GET /api/v1/business/quotes/{}", quoteId);
        return ResponseEntity.ok(quoteService.getQuote(quoteId));
    }
}
