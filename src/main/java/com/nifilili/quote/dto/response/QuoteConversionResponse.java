package com.nifilili.quote.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QuoteConversionResponse {

    private Long conversionId;
    private Long quoteId;
    private Long orderId;
    private LocalDateTime convertedAt;
    private Long convertedBy;
}
