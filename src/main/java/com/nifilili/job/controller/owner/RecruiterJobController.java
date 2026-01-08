package com.nifilili.job.controller.owner;

import com.nifilili.job.dto.request.ChangeApplicationStatusRequest;
import com.nifilili.job.dto.request.CreateJobRequest;
import com.nifilili.job.dto.response.JobApplicationResponse;
import com.nifilili.job.service.RecruiterJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/business/jobs")
@RequiredArgsConstructor
public class RecruiterJobController {

    private final RecruiterJobService recruiterJobService;

    //Job Management
//    POST /business/jobs
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Long createJob(@RequestBody CreateJobRequest request) {
        return recruiterJobService.createJob(request);
    }
//    PUT /business/jobs/{id}
//    POST /business/jobs/{id}/close
//    POST /business/jobs/{id}/reopen

    //Screening Questions

    //Applications Pipeline
//    GET /business/jobs/{id}/applications
    @GetMapping("/{jobId}/applications")
    @PreAuthorize("isAuthenticated()")
    public List<JobApplicationResponse> getApplications(@PathVariable Long jobId) {
        return recruiterJobService.getApplications(jobId);
    }

    //    POST /applications/{id}/status
    @PostMapping("/applications/{applicationId}/status")
    @PreAuthorize("isAuthenticated()")
    public void changeStatus(
            @PathVariable Long applicationId,
            @RequestBody ChangeApplicationStatusRequest request
    ) {
        recruiterJobService.changeApplicationStatus(applicationId, request);
    }
//    GET /applications/{id}/timeline


}
