package com.nifilili.kyc.service;

import com.nifilili.business.events.BusinessClaimedEvent;
import com.nifilili.business.events.BusinessDocumentReviewRequestedEvent;
import com.nifilili.business.events.BusinessPublishRequestedEvent;
import com.nifilili.kyc.dto.request.ReviewKycRequest;

public interface KycService {

    void uploadDocument(BusinessDocumentReviewRequestedEvent event);

    void submit(BusinessPublishRequestedEvent event);

    void review(Long businessId, ReviewKycRequest request);

    /**
     * Handles a business claim event by creating an initial KYC record.
     * The claimant can then upload documents and submit for review via existing endpoints.
     *
     * @param event the claim event containing businessId and claimedByUserId
     */
    void onBusinessClaimed(BusinessClaimedEvent event);
}
