package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(
        name = "CreateMunicipalityRequest",
        description = "Payload used by admins to create or update a municipality under a district."
)
public class CreateMunicipalityRequest {

    @Schema(description = "Parent district identifier.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long districtId;

    @Schema(description = "Municipality display name.", example = "Kathmandu Metropolitan City",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;

    @Schema(description = "Municipality type.", example = "metropolitan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String type;
}
