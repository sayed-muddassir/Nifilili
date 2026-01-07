package com.nifilili.business.service;

import com.nifilili.business.dto.request.SubmitBusinessForReviewRequest;
import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;

public interface BusinessPublishService {

    // Upload documents for business verification
    void uploadVerificationDocuments(Long businessId, UploadBusinessDocumentRequest request);

    // Submit business for verification
    void submitForVerification(Long businessId, SubmitBusinessForReviewRequest request);
}
