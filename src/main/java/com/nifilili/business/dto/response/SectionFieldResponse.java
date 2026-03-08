package com.nifilili.business.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(
        name = "SectionFieldResponse",
        description = "Metadata describing one input field inside a business onboarding section."
)
public class SectionFieldResponse {

    @Schema(description = "Unique identifier of the section field.", example = "71")
    private Long id;

    @Schema(description = "Identifier of the parent section.", example = "21")
    private Long sectionId;

    @Schema(description = "Internal field name used as the key in saved section data.", example = "pan_no")
    private String name;

    @Schema(description = "Human-readable field label shown to users.", example = "PAN Number")
    private String label;

    @Schema(description = "Field type such as text, number, checkbox, dropdown, or media_url.", example = "text")
    private String type;

    @ArraySchema(
            arraySchema = @Schema(description = "Selectable options for option-based field types.", nullable = true),
            schema = @Schema(example = "VAT Registered")
    )
    private List<String> options;

    @Schema(description = "Whether the field is required.", example = "true")
    private boolean required;

    @Schema(description = "Whether the field accepts multiple values.", example = "false")
    private boolean allowMultiple;
}
