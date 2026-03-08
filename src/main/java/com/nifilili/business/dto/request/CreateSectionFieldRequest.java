package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(
        name = "CreateSectionFieldRequest",
        description = "Payload used by admins to create or update a field inside a business onboarding section."
)
public class CreateSectionFieldRequest {

    @Schema(description = "Internal field name used as the key when saving section data.", example = "pan_no")
    private String name;

    @Schema(description = "Human-readable field label shown in forms.", example = "PAN Number")
    private String label;

    @Schema(description = "Field type such as text, number, checkbox, dropdown, or media_url.", example = "text")
    private String type;

    @ArraySchema(
            arraySchema = @Schema(description = "Allowed options when the field type supports predefined values.", nullable = true),
            schema = @Schema(example = "VAT Registered")
    )
    private List<String> options;

    @Schema(description = "Whether the field must be provided by the user.", example = "true")
    private boolean required;

    @Schema(description = "Whether the field accepts multiple values.", example = "false")
    private boolean allowMultiple;
}
