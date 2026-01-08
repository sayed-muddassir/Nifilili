package com.nifilili.job.service;

import com.nifilili.job.dto.request.ChangeApplicationStatusRequest;
import com.nifilili.job.dto.request.CreateJobRequest;
import com.nifilili.job.dto.response.JobApplicationResponse;

import java.util.List;

public interface RecruiterJobService {

    Long createJob(CreateJobRequest request);

    List<JobApplicationResponse> getApplications(Long jobId);

    void changeApplicationStatus(Long applicationId, ChangeApplicationStatusRequest request);

}
