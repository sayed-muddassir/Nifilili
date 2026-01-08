package com.nifilili.job.controller.publicapi;

import com.nifilili.job.dto.request.ApplyJobRequest;
import com.nifilili.job.dto.response.MyApplicationResponse;
import com.nifilili.job.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping("/{jobId}/apply")
    @PreAuthorize("isAuthenticated()")
    public void applyForJob(
            @PathVariable Long jobId,
            @RequestBody ApplyJobRequest request
    ) {
        jobApplicationService.apply(jobId, request);
    }

    @PostMapping("/applications/{applicationId}/withdraw")
    @PreAuthorize("isAuthenticated()")
    public void withdrawApplication(
            @PathVariable Long applicationId,
            @RequestParam String reason
    ) {
        jobApplicationService.withdraw(applicationId, reason);
    }

    @GetMapping("/applications/me")
    @PreAuthorize("isAuthenticated()")
    public List<MyApplicationResponse> myApplications() {
        return jobApplicationService.getMyApplications();
    }
}
