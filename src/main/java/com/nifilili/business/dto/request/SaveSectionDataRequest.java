package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
@Schema(
        name = "SaveSectionDataRequest",
        description = "Payload used to save one section block of onboarding data for a business."
)
public class SaveSectionDataRequest {

    @Schema(description = "Existing section group identifier when updating a repeatable block. Leave null to create a new group.", example = "301")
    private Long sectionGroupId; // null if new group

    @Schema(description = "Map of section field names to submitted values for the section block.", example = "{\"business_registration_no\":\"REG-2026-001\",\"pan_no\":\"123456789\"}")
    @NotNull
    private Map<String, Object> fieldValues;
}

//fieldValues keys must match section_fields.name
