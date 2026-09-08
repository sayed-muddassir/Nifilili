package com.nifilili.business.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Schema(description = "Unique identifier of the vertical to which the section belongs.", example = "1")
    private Long verticalId;

    @Schema(description = "Unique identifier of the category to which the section belongs.", example = "1")
    private Long categoryId;

    @Schema(description = "Internal section name.", example = "business_identity")
    private String name;

    @Schema(description = "Label for the section.", example = "Business Identity")
    private String label;

    @Schema(description = "Prompt for the section.", example = "Please provide your business identity information.")
    private String prompt;

    @Schema(description = "Indicates if the section is required.", example = "true")
    private boolean required;

    @Schema(description = "Indicates if the section allows multiple values.", example = "true")
    private boolean allowMultiple;

    @Schema(description = "Indicates if the section is groupable.", example = "true")
    private boolean groupable;

    @Schema(description = "List of grouped field-value maps stored for the section. Each entry represents one repeatable block.", example = "[{\"business_registration_no\":\"REG-2026-001\",\"pan_no\":\"123456789\"}]")
    @JsonIgnore
    private List<Map<String, Object>> values;
}
