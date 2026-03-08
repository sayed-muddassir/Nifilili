package com.nifilili.quote.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QuoteResponse {

    private Long quoteId;
    private Long requestId;
    private Long parentQuoteId;
    private String quoteNumber;
    private String serviceDetails;
    private BigDecimal totalAmount;
    private String currency;
    private Integer estimatedDurationDays;
    private LocalDateTime validUntil;
    private List<String> attachments;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
    private List<QuoteLineItemResponse> lineItems;
    private LocalDateTime createdAt;
}
