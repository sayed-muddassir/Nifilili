package com.nifilili.job.dto.request;

import com.nifilili.core.enums.job.JobType;

import java.time.LocalDate;
import java.util.List;

public record CreateJobRequest(
        Long businessId,
        Long jobCategoryId,
        String title,
        String description,
        JobType jobType,
        Long municipalityId,
        Long wardNumber,
        String toleName,
        String postalCode,
        boolean remote,
        Double salaryRangeMin,
        Double salaryRangeMax,
        Integer numberOfOpenings,
        LocalDate applicationDeadline,
        List<String> skills
) {
}
