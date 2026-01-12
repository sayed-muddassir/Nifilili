package com.nifilili.job.dto.response;

import com.nifilili.core.enums.job.JobApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class JobApplicationTimelineResponse {

    private JobApplicationStatus status;
    private String notes;
    private Long changedBy;
    private Instant changedDate;
}
