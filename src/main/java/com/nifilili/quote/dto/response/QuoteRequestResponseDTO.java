package com.nifilili.quote.dto.response;

import com.nifilili.core.enums.quote.QuoteRequestStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class QuoteRequestResponseDTO {

    private Long id;
    private String requestNumber;

    private Long offeringId;
    private Long businessId;

    private String requirements;
    private String budgetRange;

    private LocalDate preferredTimeline;

    private QuoteRequestStatus status;

    private LocalDateTime submittedAt;
    private LocalDateTime expiredAt;
}

