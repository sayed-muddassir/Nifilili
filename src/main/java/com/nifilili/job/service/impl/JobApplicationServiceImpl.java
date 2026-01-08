package com.nifilili.job.service.impl;

import com.nifilili.job.dto.request.ApplyJobRequest;
import com.nifilili.job.dto.response.MyApplicationResponse;
import com.nifilili.job.service.JobApplicationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobApplicationServiceImpl implements JobApplicationService {

    public void apply(Long jobId, ApplyJobRequest request) {
        // TODO:
        // 1. validate job is open
        // 2. validate user not already applied
        // 3. create JobApplication
        // 4. save answers
        // 5. create status history
    }

    public void withdraw(Long applicationId, String reason) {
        // TODO:
        // 1. load application
        // 2. ensure status == RECEIVED
        // 3. mark withdrawn
        // 4. save status history
    }

    public List<MyApplicationResponse> getMyApplications() {
        // TODO: fetch by logged-in user
        return List.of();
    }
}
