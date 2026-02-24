package com.nifilili.business.dto.request;

import lombok.Data;

@Data
public class CreateSectionRequest {

    private Long verticalId;
    private Long categoryId;

    private String name;
    private String label;
    // Legacy input retained for backward compatibility.
    private String prompt;
    private String promptText;

    private boolean required;
    private boolean allowMultiple;
    private boolean groupable;
}
