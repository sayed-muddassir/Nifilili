package com.nifilili.kyc.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DocumentReviewDecisionRequest {

    @NotNull(message = "Business document ID is required")
    private Long businessDocumentId;

    private boolean approve;

    @Size(max = 2000, message = "Rejection reason must not exceed 2000 characters")
    private String rejectionReason;
}
