package com.nifilili.job.dto.request;

import java.util.Map;

public record ApplyJobRequest(
        String resumeUrl,
        String coverLetter,
        Map<Long, String> answers
) {
}
