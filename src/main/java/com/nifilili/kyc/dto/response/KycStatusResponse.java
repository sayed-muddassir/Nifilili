package com.nifilili.kyc.dto.response;

import com.nifilili.core.enums.KycStatus;
import lombok.Data;

@Data
public class KycStatusResponse {

    private Long businessId;
    private KycStatus status;
    private String adminMessage;
    private Integer submissionCount;
}
