package com.nifilili.job.service;

import com.nifilili.job.dto.request.ApplyJobRequest;
import com.nifilili.job.dto.response.MyApplicationResponse;

import java.util.List;

public interface JobApplicationService {

    void apply(Long jobId, ApplyJobRequest request);

    void withdraw(Long applicationId, String reason);

    List<MyApplicationResponse> getMyApplications();
}
