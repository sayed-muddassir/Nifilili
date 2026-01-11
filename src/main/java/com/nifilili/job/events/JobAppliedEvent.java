package com.nifilili.job.events;

public record JobAppliedEvent(
        Long jobOpeningId,
        Long applicationId,
        Long userId
) {}

