package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(
        name = "CreateProvinceRequest",
        description = "Payload used by admins to create or update a province master record."
)
public class CreateProvinceRequest {

    @Schema(description = "Province display name.", example = "Bagmati", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;
}
