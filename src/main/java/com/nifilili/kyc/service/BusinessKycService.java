package com.nifilili.kyc.service;

import com.nifilili.common.enums.KycStatus;
import org.springframework.stereotype.Service;

@Service
public class BusinessKycService {

    public void validateSubmissionEligibility(Long businessId) {
        // Validate required docs, sections, attributes
    }

    public void createOrUpdateKyc(Long businessId, KycStatus status) {
        // Create or update business_kyc record
    }
}
