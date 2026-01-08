package com.nifilili.job.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateJobCategoryRequest(
        @NotBlank String name
) {
}
