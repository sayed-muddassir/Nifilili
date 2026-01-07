package com.nifilili.kyc.service;

import com.nifilili.core.enums.KycStatus;
import com.nifilili.kyc.dto.response.AdminKycDetailResponse;
import com.nifilili.kyc.dto.response.AdminKycListItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminKycQueryService {

    Page<AdminKycListItemResponse> getByStatus(
            KycStatus status,
            Pageable pageable
    );

    AdminKycDetailResponse getDetail(Long businessId);
}
