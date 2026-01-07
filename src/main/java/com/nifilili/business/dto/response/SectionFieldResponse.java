package com.nifilili.business.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SectionFieldResponse {

    private Long id;
    private Long sectionId;

    private String name;
    private String label;
    private String type;

    private List<String> options;

    private boolean required;
    private boolean allowMultiple;
}
