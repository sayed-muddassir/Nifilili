package com.nifilili.quote.controller.business;

import com.nifilili.quote.dto.request.CreateQuoteDTO;
import com.nifilili.quote.dto.response.QuoteRequestResponseDTO;
import com.nifilili.quote.dto.response.QuoteResponseDTO;
import com.nifilili.quote.service.QuoteRequestService;
import com.nifilili.quote.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/business/quotes")
@RequiredArgsConstructor
public class QuoteManagementController {

    private final QuoteRequestService quoteRequestService;
    private final QuoteService quoteService;

    @GetMapping("/inbox")
    public List<QuoteRequestResponseDTO> listInbox(@RequestParam Long businessId) {
        return quoteRequestService.listBusinessRequests(businessId);
    }

    @PostMapping("/requests/{requestId}/decline")
    public QuoteRequestResponseDTO declineRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String reason
    ) {
        return quoteRequestService.declineRequest(requestId, reason);
    }

    @PostMapping("/requests/{requestId}/draft")
    public QuoteResponseDTO createDraft(
            @PathVariable Long requestId,
            @Valid @RequestBody CreateQuoteDTO dto
    ) {
        return quoteService.createDraft(requestId, dto);
    }

    @PostMapping("/{quoteId}/send")
    public QuoteResponseDTO sendQuote(@PathVariable Long quoteId) {
        return quoteService.sendQuote(quoteId);
    }

    @PostMapping("/{quoteId}/revise")
    public QuoteResponseDTO reviseQuote(
            @PathVariable Long quoteId,
            @Valid @RequestBody CreateQuoteDTO dto
    ) {
        return quoteService.reviseQuote(quoteId, dto);
    }
}
