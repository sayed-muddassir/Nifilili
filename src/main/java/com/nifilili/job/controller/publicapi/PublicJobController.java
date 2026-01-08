package com.nifilili.job.controller.publicapi;

import com.nifilili.job.dto.response.JobDetailsResponse;
import com.nifilili.job.dto.response.JobSummaryResponse;
import com.nifilili.job.service.JobQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/jobs")
@RequiredArgsConstructor
public class PublicJobController {

    private final JobQueryService jobQueryService;

    @GetMapping
    public List<JobSummaryResponse> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        return jobQueryService.searchJobs(keyword, categoryId);
    }

    @GetMapping("/{jobId}")
    public JobDetailsResponse getJobDetails(@PathVariable Long jobId) {
        return jobQueryService.getJobDetails(jobId);
    }
}
