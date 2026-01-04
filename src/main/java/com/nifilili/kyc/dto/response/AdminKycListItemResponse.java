package com.nifilili.kyc.dto.response;

import com.nifilili.common.enums.KycStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminKycListItemResponse {

    private Long businessId;
    private String businessName;
    private Long verticalId;

    private KycStatus kycStatus;
    private Integer submissionCount;

    private LocalDateTime lastUpdatedAt;
}
