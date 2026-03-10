package com.nifilili.business.dto.request;

import com.nifilili.core.enums.business.BusinessAttributeFieldType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(
        name = "CreateAttributeDefinitionRequest",
        description = "Payload used by admins to create or update a reusable business attribute definition."
)
public class CreateAttributeDefinitionRequest {

    @NotNull
    @Schema(description = "Identifier of the vertical this attribute applies to.", example = "1", nullable = true)
    private Long verticalId;

    @NotBlank
    @Schema(description = "Internal attribute name.", example = "service_modes")
    private String name;

    @NotBlank
    @Schema(description = "Human-readable label shown for the attribute.", example = "Service Modes")
    private String label;

    @NotNull
    @Schema(description = "Attribute type such as text, number, checkbox, dropdown, or boolean.")
    private BusinessAttributeFieldType type;

    @NotNull
    @ArraySchema(
            arraySchema = @Schema(description = "Allowed options for selectable attribute types."),
            schema = @Schema(example = "Delivery Available")
    )
    private List<String> options;

    @Schema(description = "Whether the attribute is mandatory for businesses in the vertical.", defaultValue = "false")
    private boolean required;

    @Schema(description = "Whether multiple values can be selected for the attribute.", defaultValue = "false")
    private boolean allowMultiple;

    @NotBlank
    @Schema(description = "Prompt text used to guide users when providing the attribute value.", example = "Select all service modes your business supports.", nullable = true)
    private String prompt;
}
