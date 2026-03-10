package com.nifilili.business.dto.request;

import com.nifilili.core.enums.business.BusinessSectionFieldType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(
        name = "CreateSectionFieldRequest",
        description = "Payload used by admins to create or update a field inside a business onboarding section."
)
public class CreateSectionFieldRequest {

    @NotBlank
    @Schema(description = "Internal field name used as the key when saving section data.", example = "pan_no")
    private String name;

    @NotBlank
    @Schema(description = "Human-readable field label shown in forms.", example = "PAN Number")
    private String label;

    @NotNull
    @Schema(description = "Field type such as text, number, checkbox, dropdown, or media_url.")
    private BusinessSectionFieldType type;

    @NotNull
    @ArraySchema(
            arraySchema = @Schema(description = "Allowed options when the field type supports predefined values."),
            schema = @Schema(example = "VAT Registered")
    )
    private List<String> options;

    @Schema(description = "Whether the field must be provided by the user.", example = "true", defaultValue = "false")
    private boolean required;

    @Schema(description = "Whether the field accepts multiple values.", example = "false", defaultValue = "false")
    private boolean allowMultiple;
}
