package com.nifilili.job.service.impl;

import com.nifilili.job.dto.response.JobDetailsResponse;
import com.nifilili.job.dto.response.JobSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobQueryServiceImpl {

    public List<JobSummaryResponse> searchJobs(String keyword, Long categoryId) {
        // TODO: implement search logic
        return List.of();
    }

    public JobDetailsResponse getJobDetails(Long jobId) {
        // TODO: fetch job, questions, company info
        return null;
    }
}
