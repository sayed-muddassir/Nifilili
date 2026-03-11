package com.nifilili.quote.controller.admin;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.quote.dto.response.QuoteRequestResponse;
import com.nifilili.quote.dto.response.QuoteResponse;
import com.nifilili.quote.service.QuoteRequestService;
import com.nifilili.quote.service.QuoteService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/quotes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = SwaggerConstants.QUOTE_3, description = "Admin quote oversight endpoints")
@Hidden
public class AdminQuoteController {

    private final QuoteRequestService quoteRequestService;
    private final QuoteService quoteService;

    @GetMapping("/requests/{requestId}")
    @Operation(summary = "Get any quote request",
            description = "Admin endpoint to view any quote request by ID")
    @ApiResponse(responseCode = "200", description = "Quote request returned")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not authorized")
    @ApiResponse(responseCode = "404", description = "Quote request not found")
    public ResponseEntity<QuoteRequestResponse> getRequest(@PathVariable Long requestId) {
        log.info("GET /api/v1/admin/quotes/requests/{}", requestId);
        return ResponseEntity.ok(quoteRequestService.getRequest(requestId));
    }

    @GetMapping("/{quoteId}")
    @Operation(summary = "Get any quote",
            description = "Admin endpoint to view any quote by ID with line items")
    @ApiResponse(responseCode = "200", description = "Quote returned")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not authorized")
    @ApiResponse(responseCode = "404", description = "Quote not found")
    public ResponseEntity<QuoteResponse> getQuote(@PathVariable Long quoteId) {
        log.info("GET /api/v1/admin/quotes/{}", quoteId);
        return ResponseEntity.ok(quoteService.getQuote(quoteId));
    }
}
