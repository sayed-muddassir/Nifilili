package com.nifilili.job.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobQuestionResponse {

    private Long id;
    private String questionText;
    private boolean required;
}
