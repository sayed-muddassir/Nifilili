package com.nifilili.business.dto.response;

import com.nifilili.core.enums.BusinessStatus;
import com.nifilili.core.enums.KycStatus;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SubmitBusinessResponse {

    private Long businessId;
    private BusinessStatus status; // PENDING
    private KycStatus kycStatus;
    private String message;
}
