package com.nifilili.quote.dto.response;

import com.nifilili.core.enums.quote.QuoteStatus;
import com.nifilili.quote.dto.request.QuoteLineItemDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuoteResponseDTO {

    private Long id;
    private String quoteNumber;

    private Long requestId;

    private String serviceDetails;
    private BigDecimal totalAmount;
    private String currency;

    private Integer estimatedDurationDays;
    private LocalDateTime validUntil;

    private QuoteStatus status;

    private List<QuoteLineItemDTO> lineItems;

    private LocalDateTime sentAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
}
