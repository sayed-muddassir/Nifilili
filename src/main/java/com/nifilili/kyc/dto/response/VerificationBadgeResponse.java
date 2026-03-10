package com.nifilili.kyc.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerificationBadgeResponse {

    private Long businessId;
    private boolean verified;
}
