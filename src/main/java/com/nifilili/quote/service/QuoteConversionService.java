package com.nifilili.quote.service;

import com.nifilili.quote.dto.response.QuoteConversionResponse;

import java.util.Optional;

public interface QuoteConversionService {

    /**
     * Records a quote-to-order conversion. Idempotent — skips if already recorded.
     *
     * @param quoteId the accepted quote ID
     * @param orderId the created order ID
     * @param convertedBy the user who triggered the conversion
     */
    void recordConversion(Long quoteId, Long orderId, Long convertedBy);

    /**
     * Retrieves the conversion record for a quote, if it exists.
     *
     * @param quoteId the quote ID
     * @return the conversion details, or empty if not yet converted
     */
    Optional<QuoteConversionResponse> getConversion(Long quoteId);
}
