package com.nifilili.job.dto.request;

import com.nifilili.core.enums.job.JobApplicationStatus;

public record ChangeApplicationStatusRequest(
        JobApplicationStatus status,
        String notes
) {
}
