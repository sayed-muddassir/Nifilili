package com.nifilili.job.controller.publicapi;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.job.dto.response.JobDetailsResponse;
import com.nifilili.job.dto.response.JobSummaryResponse;
import com.nifilili.job.service.JobQueryService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/public/jobs")
@RequiredArgsConstructor
@Tag(name = SwaggerConstants.JOB_3, description = "Public job search and detail APIs.")
@Hidden
public class PublicJobController {

    private final JobQueryService jobQueryService;

    @Operation(
            summary = "Step 3.1: Search Job Openings",
            description = "Searches open job openings with optional keyword and category filtering. Returns only OPEN jobs."
    )
    @ApiResponse(responseCode = "200", description = "List of matching job summaries")
    @GetMapping
    public List<JobSummaryResponse> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        log.info("Public job search request: keyword='{}', categoryId='{}'", keyword, categoryId);
        return jobQueryService.searchJobs(keyword, categoryId);
    }

    @Operation(
            summary = "Step 3.2: Get Job Details",
            description = "Retrieves full details for a specific job opening including category, location, salary, and skills."
    )
    @ApiResponse(responseCode = "200", description = "Job opening details")
    @ApiResponse(responseCode = "404", description = "Job opening not found")
    @GetMapping("/{jobId}")
    public JobDetailsResponse getJobDetails(@PathVariable Long jobId) {
        log.info("Get job details request for jobId='{}'", jobId);
        return jobQueryService.getJobDetails(jobId);
    }
}
