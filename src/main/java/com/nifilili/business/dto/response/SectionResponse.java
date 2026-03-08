package com.nifilili.business.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Schema(
        name = "SectionResponse",
        description = "Resolved business section data, including one or more grouped value blocks."
)
public class SectionResponse {

    @Schema(description = "Unique identifier of the section definition.", example = "21")
    private Long id;

    @Schema(description = "Internal section name.", example = "business_identity")
    private String name;

    @Schema(description = "List of grouped field-value maps stored for the section. Each entry represents one repeatable block.", example = "[{\"business_registration_no\":\"REG-2026-001\",\"pan_no\":\"123456789\"}]")
    private List<Map<String, Object>> values;
}
