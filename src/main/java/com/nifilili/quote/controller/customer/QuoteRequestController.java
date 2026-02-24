package com.nifilili.quote.controller.customer;

import com.nifilili.core.security.SecurityUtil;
import com.nifilili.quote.dto.request.CreateQuoteRequestDTO;
import com.nifilili.quote.dto.response.QuoteRequestResponseDTO;
import com.nifilili.quote.dto.response.QuoteResponseDTO;
import com.nifilili.quote.service.QuoteRequestService;
import com.nifilili.quote.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quote-requests")
@RequiredArgsConstructor
public class QuoteRequestController {

    private final QuoteRequestService quoteRequestService;
    private final QuoteService quoteService;

    @PostMapping
    public QuoteRequestResponseDTO createRequest(
            @Valid @RequestBody CreateQuoteRequestDTO dto
    ) {
        return quoteRequestService.createRequest(dto);
    }

    @GetMapping("/mine")
    public List<QuoteRequestResponseDTO> myRequests() {
        return quoteRequestService.listMyRequests();
    }

    @GetMapping("/{requestId}/quotes")
    public List<QuoteResponseDTO> listQuotes(@PathVariable Long requestId) {
        return quoteService.listQuotesForRequest(requestId);
    }

    @GetMapping("/{requestId}/quotes/latest")
    public QuoteResponseDTO latestQuote(@PathVariable Long requestId) {
        return quoteService.getLatestQuote(requestId);
    }

    @PostMapping("/quotes/{quoteId}/accept")
    public QuoteResponseDTO acceptQuote(@PathVariable Long quoteId) {
        return quoteService.acceptQuote(quoteId, SecurityUtil.getCurrentUserId());
    }

    @PostMapping("/quotes/{quoteId}/reject")
    public QuoteResponseDTO rejectQuote(
            @PathVariable Long quoteId,
            @RequestParam(required = false) String reason
    ) {
        return quoteService.rejectQuote(quoteId, SecurityUtil.getCurrentUserId(), reason);
    }
}
