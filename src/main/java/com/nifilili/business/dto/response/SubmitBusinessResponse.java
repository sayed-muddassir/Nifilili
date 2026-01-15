package com.nifilili.business.dto.response;

import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.enums.kyc.KycStatus;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SubmitBusinessResponse {

    private Long businessId;
    private BusinessStatus status; // PENDING
    private KycStatus kycStatus;
    private String message;
}
