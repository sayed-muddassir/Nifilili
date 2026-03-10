package com.nifilili.kyc.service;

import com.nifilili.core.enums.kyc.KycStatus;
import com.nifilili.kyc.dto.response.AdminKycDetailResponse;
import com.nifilili.kyc.dto.response.AdminKycListItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminKycQueryService {

    /**
     * Retrieves a paginated list of KYC applications filtered by status.
     *
     * @param status   the KYC status to filter by (PENDING, APPROVED, REJECTED, NOT_STARTED)
     * @param pageable pagination parameters
     * @return paginated list of KYC summary items
     */
    Page<AdminKycListItemResponse> getByStatus(
            KycStatus status,
            Pageable pageable
    );

    /**
     * Retrieves detailed KYC information for a specific business, including uploaded documents.
     *
     * @param businessId the business ID
     * @return full KYC detail with documents and business metadata
     * @throws com.nifilili.core.exception.ResourceNotFoundException if KYC record not found
     */
    AdminKycDetailResponse getDetail(Long businessId);
}
