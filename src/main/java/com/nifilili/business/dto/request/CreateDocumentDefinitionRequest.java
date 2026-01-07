package com.nifilili.business.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateDocumentDefinitionRequest {

    private Long verticalId;

    private String name;
    private String label;

    private List<String> allowedExtensions;
    private Integer maxFileSize;

    private boolean required;
}
