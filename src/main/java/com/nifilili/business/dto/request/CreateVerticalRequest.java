package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(
        name = "CreateVerticalRequest",
        description = "Payload used by admins to create or update a top-level business vertical."
)
public class CreateVerticalRequest {

    @NotBlank
    @Schema(description = "Display name of the vertical.", example = "Retail")
    private String name;

    @NotBlank
    @Schema(description = "URL-safe slug for the vertical.", example = "retail")
    private String slug;

    @NotBlank
    @Schema(description = "Short description of the vertical and its scope.", example = "Retail businesses including storefronts and online sellers.", nullable = true)
    private String description;

    @NotBlank
    @Schema(description = "Icon URL representing the vertical in the onboarding UI.", example = "https://cdn.nifilili.com/icons/retail.svg", nullable = true)
    private String iconUrl;

    @Schema(description = "Whether the vertical is active and available for onboarding.", example = "true", defaultValue = "true")
    private boolean isActive;
}
