package com.nifilili.quote.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class QuoteRequestResponse {

    private Long requestId;
    private Long offeringId;
    private Long userId;
    private Long businessId;
    private String requestNumber;
    private String requirements;
    private String budgetRange;
    private LocalDate preferredTimeline;
    private Map<String, Object> deliveryAddress;
    private List<String> attachments;
    private String status;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
}
