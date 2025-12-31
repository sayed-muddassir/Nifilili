package com.nifilili.config.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateSectionFieldRequest {

    private String name;
    private String label;
    private String type;

    private List<String> options;

    private boolean required;
    private boolean allowMultiple;
}
