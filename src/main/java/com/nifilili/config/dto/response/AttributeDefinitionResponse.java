package com.nifilili.config.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AttributeDefinitionResponse {

    private Long id;
    private Long verticalId;

    private String name;
    private String label;
    private String type;

    private List<String> options;

    private boolean required;
    private boolean allowMultiple;

    private String prompt;
}
