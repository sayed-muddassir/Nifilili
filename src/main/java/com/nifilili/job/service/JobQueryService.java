package com.nifilili.job.service;

import com.nifilili.job.dto.response.JobDetailsResponse;
import com.nifilili.job.dto.response.JobSummaryResponse;

import java.util.List;

public interface JobQueryService {

    /**
     * Searches for open job openings with optional keyword and category filtering.
     * Only returns jobs with OPEN status, ordered by creation date descending.
     *
     * @param keyword    optional text to match against job title or description (case-insensitive)
     * @param categoryId optional job category ID to filter by
     * @return list of matching job summaries
     */
    List<JobSummaryResponse> searchJobs(String keyword, Long categoryId);

    /**
     * Retrieves full details for a specific job opening.
     *
     * @param jobId the ID of the job opening
     * @return the job details including category, location, salary, and skills
     * @throws com.nifilili.core.exception.ResourceNotFoundException if the job opening does not exist
     */
    JobDetailsResponse getJobDetails(Long jobId);
}
