package com.nifilili.job.controller.publicapi;

import com.nifilili.job.dto.request.ApplyJobRequest;
import com.nifilili.job.dto.response.MyApplicationResponse;
import com.nifilili.job.service.JobApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Application Management", description = "APIs for job seekers to manage their job applications")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping("/{jobId}/apply")
//    @PreAuthorize("isAuthenticated()")
    public Long applyForJob(
            @PathVariable Long jobId,
            @RequestBody ApplyJobRequest request
    ) {
        return jobApplicationService.apply(jobId, request);
    }

    @PostMapping("/applications/{applicationId}/withdraw")
//    @PreAuthorize("isAuthenticated()")
    public void withdrawApplication(
            @PathVariable Long applicationId,
            @RequestParam String reason
    ) {
        jobApplicationService.withdraw(applicationId, reason);
    }

    @GetMapping("/applications/{userId}")
//    @PreAuthorize("isAuthenticated()")
    public List<MyApplicationResponse> myApplications(@PathVariable("userId") Long userId) {
        return jobApplicationService.getMyApplications(userId);
    }
}
