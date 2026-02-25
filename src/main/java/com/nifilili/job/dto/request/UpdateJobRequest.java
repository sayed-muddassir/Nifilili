package com.nifilili.job.dto.request;

import com.nifilili.core.enums.job.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record UpdateJobRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull JobType jobType,
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
