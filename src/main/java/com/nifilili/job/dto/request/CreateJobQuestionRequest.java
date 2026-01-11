package com.nifilili.job.dto.request;

public record CreateJobQuestionRequest(
        String questionText,
        boolean required
) {
}
