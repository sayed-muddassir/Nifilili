package com.nifilili.job.controller.owner;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.job.dto.request.ChangeApplicationStatusRequest;
import com.nifilili.job.dto.request.CreateJobQuestionRequest;
import com.nifilili.job.dto.request.CreateJobRequest;
import com.nifilili.job.dto.request.UpdateJobRequest;
import com.nifilili.job.dto.response.JobApplicationResponse;
import com.nifilili.job.dto.response.JobApplicationTimelineResponse;
import com.nifilili.job.dto.response.JobQuestionResponse;
import com.nifilili.job.service.RecruiterJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/business/jobs")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('USER')")
@Tag(name = SwaggerConstants.JOB_2, description = "Recruiter APIs for job lifecycle and hiring pipeline.")
public class RecruiterJobController {

    private final RecruiterJobService recruiterJobService;

    // ── Job Lifecycle ───────────────────────────────────────────────────────

    @Operation(
            summary = "Step 2.1: Create Job Opening",
            description = "Creates a new job opening in DRAFT status for the specified business."
    )
    @ApiResponse(responseCode = "201", description = "Job opening created, returns the new job ID")
    @ApiResponse(responseCode = "400", description = "Invalid business ID or job category ID")
    @PostMapping
    public ResponseEntity<Long> createJob(@Valid @RequestBody CreateJobRequest request) {
        log.info("Create job request for businessId='{}'", request.businessId());
        Long jobId = recruiterJobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobId);
    }

    @Operation(
            summary = "Step 2.2: Update Job Opening",
            description = "Updates a job opening that is still in DRAFT status. Only editable before publishing."
    )
    @ApiResponse(responseCode = "200", description = "Job opening updated successfully")
    @ApiResponse(responseCode = "400", description = "Job is not in DRAFT status or validation failed")
    @ApiResponse(responseCode = "404", description = "Job opening not found")
    @PutMapping("/{jobId}")
    public ResponseEntity<Void> updateJob(
            @PathVariable Long jobId,
            @Valid @RequestBody UpdateJobRequest request
    ) {
        log.info("Update job request received for jobId='{}'", jobId);
        recruiterJobService.updateJob(jobId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Step 2.4: Publish Job Opening",
            description = "Publishes a DRAFT job opening, changing its status to OPEN and making it visible to applicants."
    )
    @ApiResponse(responseCode = "200", description = "Job opening published successfully")
    @ApiResponse(responseCode = "400", description = "Job is not in DRAFT status")
    @PostMapping("/{jobId}/publish")
    public ResponseEntity<Void> publishJob(@PathVariable Long jobId) {
        log.info("Publish job request for jobId='{}'", jobId);
        recruiterJobService.publishJob(jobId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Step 2.9: Close Job Opening",
            description = "Closes an open job opening, hiding it from public listings and preventing new applications."
    )
    @ApiResponse(responseCode = "200", description = "Job opening closed successfully")
    @ApiResponse(responseCode = "400", description = "Job is not in OPEN status")
    @ApiResponse(responseCode = "404", description = "Job opening not found")
    @PostMapping("/{jobId}/close")
    public ResponseEntity<Void> closeJob(@PathVariable Long jobId) {
        log.info("Close job request received for jobId='{}'", jobId);
        recruiterJobService.closeJob(jobId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Step 2.10: Reopen Job Opening",
            description = "Reopens a previously closed job opening, making it visible to public listings again."
    )
    @ApiResponse(responseCode = "200", description = "Job opening reopened successfully")
    @ApiResponse(responseCode = "400", description = "Job is not in CLOSED status")
    @ApiResponse(responseCode = "404", description = "Job opening not found")
    @PostMapping("/{jobId}/reopen")
    public ResponseEntity<Void> reopenJob(@PathVariable Long jobId) {
        log.info("Reopen job request received for jobId='{}'", jobId);
        recruiterJobService.reopenJob(jobId);
        return ResponseEntity.ok().build();
    }

    // ── Screening Questions ─────────────────────────────────────────────────

    @Operation(
            summary = "Step 2.3: Add Screening Question",
            description = "Adds a custom screening question to a DRAFT job opening."
    )
    @ApiResponse(responseCode = "201", description = "Screening question created, returns the new question ID")
    @ApiResponse(responseCode = "400", description = "Job not found or not in DRAFT status")
    @PostMapping("/{jobId}/questions")
    public ResponseEntity<Long> createJobQuestions(
            @PathVariable("jobId") Long jobId,
            @Valid @RequestBody CreateJobQuestionRequest request
    ) {
        log.info("Add screening question for jobId='{}'", jobId);
        Long questionId = recruiterJobService.createJobQuestion(request, jobId);
        return ResponseEntity.status(HttpStatus.CREATED).body(questionId);
    }

    @Operation(
            summary = "Step 2.5: List Screening Questions",
            description = "Retrieves all screening questions for a specific job opening."
    )
    @ApiResponse(responseCode = "200", description = "List of screening questions")
    @GetMapping("/{jobId}/questions")
    public List<JobQuestionResponse> getJobQuestions(@PathVariable("jobId") Long jobId) {
        log.info("Get screening questions for jobId='{}'", jobId);
        return recruiterJobService.getJobQuestions(jobId);
    }

    // ── Application Pipeline ────────────────────────────────────────────────

    @Operation(
            summary = "Step 2.6: List Applications for Job",
            description = "Retrieves all applications submitted for a specific job opening with answers to screening questions."
    )
    @ApiResponse(responseCode = "200", description = "List of applications with answers")
    @GetMapping("/{jobId}/applications")
    public List<JobApplicationResponse> getApplications(@PathVariable Long jobId) {
        log.info("Get applications for jobId='{}'", jobId);
        return recruiterJobService.getApplicationsForJob(jobId);
    }

    @Operation(
            summary = "Step 2.7: Change Application Status",
            description = "Moves an application through the recruitment pipeline. Valid transitions: RECEIVED→REVIEWED/REJECTED, REVIEWED→SHORTLISTED/REJECTED, SHORTLISTED→INTERVIEWING, INTERVIEWING→HIRED/REJECTED."
    )
    @ApiResponse(responseCode = "200", description = "Application status updated and history recorded")
    @ApiResponse(responseCode = "400", description = "Invalid status transition or application not found")
    @PostMapping("/applications/{applicationId}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody ChangeApplicationStatusRequest request
    ) {
        log.info("Change application status: applicationId='{}'", applicationId);
        recruiterJobService.changeApplicationStatus(applicationId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Step 2.8: Get Application Timeline",
            description = "Retrieves the full audit trail of status changes for a specific application."
    )
    @ApiResponse(responseCode = "200", description = "Chronological list of status changes")
    @GetMapping("/applications/{applicationId}/timeline")
    public List<JobApplicationTimelineResponse> getApplicationTimeline(
            @PathVariable Long applicationId
    ) {
        log.info("Get application timeline: applicationId='{}'", applicationId);
        return recruiterJobService.getApplicationTimeline(applicationId);
    }
}
