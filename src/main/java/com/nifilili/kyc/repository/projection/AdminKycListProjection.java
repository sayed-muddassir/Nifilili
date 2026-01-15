package com.nifilili.kyc.repository.projection;

import com.nifilili.core.enums.kyc.KycStatus;

import java.time.LocalDateTime;

public interface AdminKycListProjection {

    Long getBusinessId();

    String getBusinessName();

    Long getVerticalId();

    KycStatus getKycStatus();

    Integer getSubmissionCount();

    LocalDateTime getUpdatedAt();
}
