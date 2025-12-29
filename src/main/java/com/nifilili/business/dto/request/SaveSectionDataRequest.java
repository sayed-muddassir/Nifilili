package com.nifilili.business.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class SaveSectionDataRequest {

    private Long sectionGroupId; // null if new group

    @NotNull
    private Map<String, Object> fieldValues;
}

//fieldValues keys must match section_fields.name
