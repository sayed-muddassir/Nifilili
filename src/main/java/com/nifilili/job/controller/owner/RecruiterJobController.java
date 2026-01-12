package com.nifilili.job.controller.owner;

import com.nifilili.job.dto.request.ChangeApplicationStatusRequest;
import com.nifilili.job.dto.request.CreateJobQuestionRequest;
import com.nifilili.job.dto.request.CreateJobRequest;
import com.nifilili.job.dto.response.JobApplicationResponse;
import com.nifilili.job.dto.response.JobApplicationTimelineResponse;
import com.nifilili.job.dto.response.JobQuestionResponse;
import com.nifilili.job.service.RecruiterJobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/business/jobs")
@RequiredArgsConstructor
@Tag(name = "Recruiter Job Management", description = "APIs for recruiters to manage job postings and applications")
public class RecruiterJobController {

    private final RecruiterJobService recruiterJobService;

    //Job Management
    @PostMapping
//    @PreAuthorize("isAuthenticated()")
    public Long createJob(@RequestBody CreateJobRequest request) {
        return recruiterJobService.createJob(request);
    }
//    PUT /business/jobs/{id}
//    POST /business/jobs/{id}/close
//    POST /business/jobs/{id}/reopen

    //Screening Questions
    @PostMapping("/{jobId}/questions")
//    @PreAuthorize("isAuthenticated()")
    public Long createJobQuestions(@PathVariable("jobId") Long jobId, @RequestBody CreateJobQuestionRequest request) {
        return recruiterJobService.createJobQuestion(request, jobId);
    }

    @GetMapping("/{jobId}/questions")
//    @PreAuthorize("isAuthenticated()")
    public List<JobQuestionResponse> getJobQuestions(@PathVariable("jobId") Long jobId) {
        return recruiterJobService.getJobQuestions(jobId);
    }

    //Publish Job so that it will be eligible for applications
    @PostMapping("/{jobId}/publish")
//    @PreAuthorize("isAuthenticated()")
    public void publishJob(@PathVariable Long jobId) {
        recruiterJobService.publishJob(jobId);
    }


    @GetMapping("/{jobId}/applications")
//    @PreAuthorize("isAuthenticated()")
    public List<JobApplicationResponse> getApplications(@PathVariable Long jobId) {
        return recruiterJobService.getApplicationsForJob(jobId);
    }

    @PostMapping("/applications/{applicationId}/status")
//    @PreAuthorize("isAuthenticated()")
    public void changeStatus(
            @PathVariable Long applicationId,
            @RequestBody ChangeApplicationStatusRequest request
    ) {
        recruiterJobService.changeApplicationStatus(applicationId, request);
    }

    @GetMapping("/applications/{applicationId}/timeline")
//    @PreAuthorize("isAuthenticated()")
    public List<JobApplicationTimelineResponse> getApplicationTimeline(
            @PathVariable Long applicationId
    ) {
        return recruiterJobService.getApplicationTimeline(applicationId);
    }

}
