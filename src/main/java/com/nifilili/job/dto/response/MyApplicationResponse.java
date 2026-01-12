package com.nifilili.job.dto.response;

import com.nifilili.core.enums.job.JobApplicationStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyApplicationResponse {

    private Long applicationId;
    private String jobTitle;
    private String jobDescription;
    private JobApplicationStatus applicationStatus;
}
