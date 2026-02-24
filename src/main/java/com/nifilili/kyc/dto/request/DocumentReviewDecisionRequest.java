package com.nifilili.kyc.dto.request;

import lombok.Data;

@Data
public class DocumentReviewDecisionRequest {

    // Existing uploaded document id from business_documents table.
    private Long businessDocumentId;
    private boolean approve;
    private String rejectionReason;
}
