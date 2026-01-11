package com.nifilili.kyc.service;

import com.nifilili.business.events.BusinessDocumentReviewRequestedEvent;
import com.nifilili.business.events.BusinessPublishRequestedEvent;
import com.nifilili.kyc.dto.request.ReviewKycRequest;

public interface KycService {

    void uploadDocument(BusinessDocumentReviewRequestedEvent event);

    void submit(BusinessPublishRequestedEvent event);

    void review(Long businessId, ReviewKycRequest request);
}
