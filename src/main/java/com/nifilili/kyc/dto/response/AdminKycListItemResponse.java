package com.nifilili.kyc.dto.response;

import com.nifilili.core.enums.kyc.KycStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class AdminKycListItemResponse {

    private Long businessId;
    private String businessName;
    private Long verticalId;

    private KycStatus kycStatus;
    private Integer submissionCount;

    private Instant lastUpdatedAt;
}
