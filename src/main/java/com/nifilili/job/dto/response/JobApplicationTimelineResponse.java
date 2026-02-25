package com.nifilili.job.dto.response;

import com.nifilili.core.enums.job.JobApplicationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class JobApplicationTimelineResponse {

    private JobApplicationStatus statusFrom;
    private JobApplicationStatus statusTo;
    private String notes;
    private Long changedBy;
    private Instant changedDate;
}
