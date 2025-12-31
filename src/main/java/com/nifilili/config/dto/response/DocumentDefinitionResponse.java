package com.nifilili.config.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DocumentDefinitionResponse {

    private Long id;
    private Long verticalId;

    private String name;
    private String label;

    private List<String> allowedExtensions;
    private Integer maxFileSize;

    private boolean required;
}
