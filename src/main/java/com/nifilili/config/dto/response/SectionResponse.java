package com.nifilili.config.dto.response;

import lombok.Data;

@Data
public class SectionResponse {

    private Long id;
    private Long verticalId;

    private String name;
    private String label;
    private String prompt;

    private boolean required;
    private boolean allowMultiple;
    private boolean groupable;
}
