package com.nifilili.job.controller.publicapi;

import com.nifilili.job.dto.request.ApplyJobRequest;
import com.nifilili.job.dto.response.MyApplicationResponse;
import com.nifilili.job.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/public/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Application Management", description = "APIs for job seekers to manage their job applications")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @Operation(
            summary = "Apply for Job",
            description = "Submits a job application with resume, cover letter, and answers to screening questions."
    )
    @ApiResponse(responseCode = "201", description = "Application submitted successfully")
    @ApiResponse(responseCode = "400", description = "Job is not open or user has already applied")
    @PostMapping("/{jobId}/apply")
    public ResponseEntity<Long> applyForJob(
            @PathVariable Long jobId,
            @Valid @RequestBody ApplyJobRequest request
    ) {
        log.info("Apply for job request: jobId='{}'", jobId);
        Long applicationId = jobApplicationService.apply(jobId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationId);
    }

    @Operation(
            summary = "Withdraw Application",
            description = "Withdraws a job application. Only allowed while the application is still in RECEIVED status."
    )
    @ApiResponse(responseCode = "200", description = "Application withdrawn successfully")
    @ApiResponse(responseCode = "400", description = "Application not found or not in withdrawable status")
    @PostMapping("/applications/{applicationId}/withdraw")
    public ResponseEntity<Void> withdrawApplication(
            @PathVariable Long applicationId,
            @RequestParam String reason
    ) {
        log.info("Withdraw application request: applicationId='{}'", applicationId);
        jobApplicationService.withdraw(applicationId, reason);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get My Applications",
            description = "Retrieves all job applications submitted by the specified user with their current status."
    )
    @ApiResponse(responseCode = "200", description = "List of user's job applications")
    @GetMapping("/applications/{userId}")
    public List<MyApplicationResponse> myApplications(@PathVariable("userId") Long userId) {
        log.info("Get my applications request: userId='{}'", userId);
        return jobApplicationService.getMyApplications(userId);
    }
}
