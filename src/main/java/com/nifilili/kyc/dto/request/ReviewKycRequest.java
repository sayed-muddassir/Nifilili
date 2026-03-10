package com.nifilili.kyc.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ReviewKycRequest {

    private boolean approve;

    @Size(max = 2000, message = "Admin message must not exceed 2000 characters")
    private String adminMessage;

    // Optional per-document decisions for precise review feedback.
    @Valid
    private List<DocumentReviewDecisionRequest> documentDecisions;
}
