package com.nifilili.kyc.repository.projection;

import java.time.Instant;

public interface AdminKycListProjection {

    Long getBusinessId();

    String getBusinessName();

    Long getVerticalId();

    String getKycStatus();

    Integer getSubmissionCount();

    Instant getUpdatedAt();
}
