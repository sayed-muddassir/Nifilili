package com.nifilili.job.dto.response;

import java.time.Instant;

public record JobCategoryResponse(
        Long id,
        String name,
        Instant createdAt
) {
}
