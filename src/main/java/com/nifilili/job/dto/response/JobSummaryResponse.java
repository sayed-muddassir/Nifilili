package com.nifilili.job.dto.response;

import com.nifilili.core.enums.job.JobType;
import lombok.Data;

@Data
public class JobSummaryResponse {

    private String title;
    private String description;
    private JobType jobType;
}
