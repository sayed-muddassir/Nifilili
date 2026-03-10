package com.nifilili.kyc.api.impl;

import com.nifilili.core.enums.kyc.KycStatus;
import com.nifilili.kyc.api.KycQueryApi;
import com.nifilili.kyc.repository.BusinessKycRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class KycQueryImpl implements KycQueryApi {

    private final BusinessKycRepository kycRepository;

    @Override
    public boolean isBusinessVerified(Long businessId) {
        return kycRepository.findByBusinessId(businessId)
                .map(kyc -> kyc.getKycStatus() == KycStatus.APPROVED)
                .orElse(false);
    }
}
