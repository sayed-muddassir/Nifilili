package com.nifilili.kyc.service;

import com.nifilili.kyc.dto.response.BusinessDocumentResponse;
import com.nifilili.kyc.dto.response.KycStatusResponse;

import java.util.List;

public interface OwnerKycQueryService {

    /**
     * Returns the current KYC status for a business, including rejection reason for the correction banner.
     *
     * @param businessId the business to check
     * @return KYC status with correction details
     * @throws com.nifilili.core.exception.ResourceNotFoundException if no KYC record exists
     * @throws com.nifilili.core.exception.InvalidBusinessStateException if caller does not own the business
     */
    KycStatusResponse getStatus(Long businessId);

    /**
     * Returns the list of documents uploaded by the business owner, with per-document review status.
     *
     * @param businessId the business to check
     * @return list of uploaded document details
     * @throws com.nifilili.core.exception.ResourceNotFoundException if business not found
     * @throws com.nifilili.core.exception.InvalidBusinessStateException if caller does not own the business
     */
    List<BusinessDocumentResponse> getDocuments(Long businessId);
}
