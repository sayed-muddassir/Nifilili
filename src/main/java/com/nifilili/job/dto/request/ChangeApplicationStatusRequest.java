package com.nifilili.job.dto.request;

public record ChangeApplicationStatusRequest(
        String status,
        String notes
) {
}
