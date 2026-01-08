package com.nifilili.job.service.impl;

import com.nifilili.job.dto.request.ChangeApplicationStatusRequest;
import com.nifilili.job.dto.request.CreateJobRequest;
import com.nifilili.job.dto.response.JobApplicationResponse;
import com.nifilili.job.service.RecruiterJobService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecruiterJobServiceImpl implements RecruiterJobService {

    public Long createJob(CreateJobRequest request) {
        // TODO:
        // 1. create JobOpening
        // 2. save screening questions
        return 1L;
    }

    public List<JobApplicationResponse> getApplications(Long jobId) {
        // TODO: fetch applications for job
        return List.of();
    }

    public void changeApplicationStatus(
            Long applicationId,
            ChangeApplicationStatusRequest request
    ) {
        // TODO:
        // 1. validate transition
        // 2. update status
        // 3. save history
        // 4. emit event
    }
}
