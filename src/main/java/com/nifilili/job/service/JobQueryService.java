package com.nifilili.job.service;

import com.nifilili.job.dto.response.JobDetailsResponse;
import com.nifilili.job.dto.response.JobSummaryResponse;

import java.util.List;

public interface JobQueryService {

    List<JobSummaryResponse> searchJobs(String keyword, Long categoryId);

    JobDetailsResponse getJobDetails(Long jobId);
}
