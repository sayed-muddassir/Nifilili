package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

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

    @Schema(description = "Municipality description.", example = "Kathmandu is the capital city of Nepal.", nullable = true)
    private String description;

    @Schema(description = "List of image URLs associated with the municipality.", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]", nullable = true)
    private List<String> imageUrls;
}
