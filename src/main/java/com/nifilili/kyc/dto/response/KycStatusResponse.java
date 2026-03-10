package com.nifilili.kyc.dto.response;

import com.nifilili.core.enums.kyc.KycStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class KycStatusResponse {

    private Long businessId;
    private KycStatus kycStatus;
    private String rejectionReason;
    private String adminMessage;
    private Integer submissionCount;
    private Instant updatedAt;
}
