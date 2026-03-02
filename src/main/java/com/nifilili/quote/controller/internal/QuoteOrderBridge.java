package com.nifilili.quote.controller.internal;

import com.nifilili.quote.repository.QuoteConversionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/quote-bridge")
@RequiredArgsConstructor
public class QuoteOrderBridge {

    private final QuoteConversionRepository quoteConversionRepository;

    @GetMapping("/{quoteId}/order-id")
    public Long findOrderIdByQuote(@PathVariable Long quoteId) {
        return quoteConversionRepository.findByQuoteId(quoteId)
                .map(conversion -> conversion.getOrderId())
                .orElse(null);
    }
}
