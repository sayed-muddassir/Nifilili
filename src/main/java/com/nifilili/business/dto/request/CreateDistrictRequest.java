package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(
        name = "CreateDistrictRequest",
        description = "Payload used by admins to create or update a district under a province."
)
public class CreateDistrictRequest {

    @Schema(description = "Parent province identifier.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long provinceId;

    @Schema(description = "District display name.", example = "Kathmandu", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;
}
