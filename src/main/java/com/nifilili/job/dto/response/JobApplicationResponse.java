package com.nifilili.job.dto.response;

import com.nifilili.core.enums.job.JobApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JobApplicationResponse {

    private Long jobApplicationId;
    private Long jobOpeningId;
    private Long userId;
    private String resumeUrl;
    private String coverLetter;
    private JobApplicationStatus status;
    private List<JobApplicationAnswerResponse> answers;
}
