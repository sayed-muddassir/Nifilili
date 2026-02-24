package com.nifilili.business.service;

import com.nifilili.business.dto.request.SubmitBusinessForReviewRequest;
import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;

public interface BusinessPublishService {

    /**
     * Uploads a KYC document for business verification.
     *
     * @param businessId ID of the business
     * @param request    document upload payload
     * @throws com.nifilili.core.exception.ResourceNotFoundException    if business does not exist
     * @throws com.nifilili.core.exception.InvalidBusinessStateException if current user is not the owner
     */
    void uploadVerificationDocuments(Long businessId, UploadBusinessDocumentRequest request);

    /**
     * Submits the business for admin verification.
     *
     * @param businessId ID of the business
     * @param request    submission payload with optional message
     * @throws com.nifilili.core.exception.ResourceNotFoundException    if business does not exist
     * @throws com.nifilili.core.exception.InvalidBusinessStateException if current user is not the owner
     */
    void submitForVerification(Long businessId, SubmitBusinessForReviewRequest request);

    /**
     * Claims an unclaimed admin-seeded business for the authenticated user.
     * Sets the current user as the owner and triggers the KYC process.
     *
     * @param businessId ID of the admin-seeded business to claim
     * @throws com.nifilili.core.exception.ResourceNotFoundException    if business does not exist
     * @throws com.nifilili.core.exception.InvalidBusinessStateException if business is not admin-seeded or already claimed
     */
    void claimBusiness(Long businessId);
}
