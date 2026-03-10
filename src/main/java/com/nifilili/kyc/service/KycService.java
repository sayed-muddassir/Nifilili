package com.nifilili.kyc.service;

import com.nifilili.business.events.BusinessClaimedEvent;
import com.nifilili.business.events.BusinessDocumentReviewRequestedEvent;
import com.nifilili.business.events.BusinessPublishRequestedEvent;
import com.nifilili.kyc.dto.request.ReviewKycRequest;

public interface KycService {

    /**
     * Handles a document upload event by creating a pending BusinessDocument record.
     *
     * @param event the event containing businessId and upload details (file URL, definition ID)
     */
    void uploadDocument(BusinessDocumentReviewRequestedEvent event);

    /**
     * Handles a KYC submission event by creating or updating the BusinessKyc record to PENDING.
     * Publishes a {@link com.nifilili.kyc.events.KycSubmittedEvent} to notify the business module.
     *
     * @param event the event containing businessId and optional message
     */
    void submit(BusinessPublishRequestedEvent event);

    /**
     * Processes an admin review decision (approve or reject) for a business KYC application.
     * Publishes {@link com.nifilili.kyc.events.KycApprovedEvent} or
     * {@link com.nifilili.kyc.events.KycRejectedEvent} accordingly.
     *
     * @param businessId the business being reviewed
     * @param request    the review decision with optional per-document feedback
     * @throws com.nifilili.core.exception.ResourceNotFoundException if KYC record not found
     * @throws com.nifilili.core.exception.InvalidKycStateException  if KYC is not in PENDING state
     */
    void review(Long businessId, ReviewKycRequest request);

    /**
     * Handles a business claim event by creating an initial KYC record with NOT_STARTED status.
     * Idempotent — skips creation if a KYC record already exists for the business.
     *
     * @param event the claim event containing businessId and claimedByUserId
     */
    void onBusinessClaimed(BusinessClaimedEvent event);
}
