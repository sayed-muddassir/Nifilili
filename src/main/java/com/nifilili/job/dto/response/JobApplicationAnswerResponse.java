package com.nifilili.job.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobApplicationAnswerResponse {

    private Long questionId;
    private String questionText;
    private String answer;
}
