package com.nifilili.config.dto.request;

import lombok.Data;

@Data
public class CreateSectionRequest {

    private Long verticalId;

    private String name;
    private String label;
    private String prompt;

    private boolean required;
    private boolean allowMultiple;
    private boolean groupable;
}
