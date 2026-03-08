package com.nifilili.kyc.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ReviewKycRequest {

    private boolean approve;
    private String adminMessage;
    // Optional per-document decisions for precise review feedback.
    private List<DocumentReviewDecisionRequest> documentDecisions;
}
