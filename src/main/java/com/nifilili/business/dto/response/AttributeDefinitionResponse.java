package com.nifilili.business.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(
        name = "AttributeDefinitionResponse",
        description = "Metadata describing a reusable business attribute definition."
)
public class AttributeDefinitionResponse {

    @Schema(description = "Unique identifier of the attribute definition.", example = "41")
    private Long id;

    @Schema(description = "Identifier of the vertical this attribute belongs to.", example = "1")
    private Long verticalId;

    @Schema(description = "Internal attribute name.", example = "service_modes")
    private String name;

    @Schema(description = "Human-readable attribute label.", example = "Service Modes")
    private String label;

    @Schema(description = "Attribute type such as text, number, checkbox, dropdown, or boolean.", example = "checkbox")
    private String type;

    @ArraySchema(
            arraySchema = @Schema(description = "Allowed options for selectable attribute types.", nullable = true),
            schema = @Schema(example = "Delivery Available")
    )
    private List<String> options;

    @Schema(description = "Whether the attribute is mandatory.", example = "false")
    private boolean required;

    @Schema(description = "Whether multiple values can be selected for the attribute.", example = "true")
    private boolean allowMultiple;

    @Schema(description = "Prompt text shown when collecting the attribute value.", example = "Select all service modes your business supports.", nullable = true)
    private String prompt;
}
