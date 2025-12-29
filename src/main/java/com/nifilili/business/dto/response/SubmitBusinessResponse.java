package com.nifilili.business.dto.response;

import com.nifilili.common.enums.BusinessStatus;
import com.nifilili.common.enums.KycStatus;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SubmitBusinessResponse {

    private Long businessId;
    private BusinessStatus status; // PENDING
    private KycStatus kycStatus;
    private String message;
}
