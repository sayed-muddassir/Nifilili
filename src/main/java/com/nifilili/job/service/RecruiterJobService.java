package com.nifilili.job.service;

import com.nifilili.job.dto.request.ChangeApplicationStatusRequest;
import com.nifilili.job.dto.request.CreateJobQuestionRequest;
import com.nifilili.job.dto.request.CreateJobRequest;
import com.nifilili.job.dto.response.JobApplicationResponse;
import com.nifilili.job.dto.response.JobQuestionResponse;

import java.util.List;

public interface RecruiterJobService {

    Long createJob(CreateJobRequest request);

    Long createJobQuestion(CreateJobQuestionRequest request, Long jobId);

    List<JobQuestionResponse> getJobQuestions(Long jobId);

    List<JobApplicationResponse> getApplications(Long jobId);

    void changeApplicationStatus(Long applicationId, ChangeApplicationStatusRequest request);

    void getApplicationTimeline(Long applicationId);

}
