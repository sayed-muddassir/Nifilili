package com.nifilili.kyc.service;

import com.nifilili.business.events.BusinessDocumentReviewRequested;
import com.nifilili.business.events.BusinessPublishRequested;
import com.nifilili.kyc.dto.request.ReviewKycRequest;

public interface KycService {

    void uploadDocument(BusinessDocumentReviewRequested event);

    void submit(BusinessPublishRequested event);

    void review(Long businessId, ReviewKycRequest request);
}
