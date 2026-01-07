package com.nifilili.business.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateAttributeDefinitionRequest {

    private Long verticalId;
    private String name;
    private String label;
    private String type;

    private List<String> options;

    private boolean required;
    private boolean allowMultiple;

    private String prompt;
}
