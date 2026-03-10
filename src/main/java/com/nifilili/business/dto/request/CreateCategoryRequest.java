package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(
        name = "CreateCategoryRequest",
        description = "Payload used by admins to create or update a business category under a vertical."
)
public class CreateCategoryRequest {

    @NotNull
    @Schema(description = "Identifier of the parent business vertical.", example = "1")
    private Long businessVerticalId;

    @Schema(description = "Optional parent category identifier for nested category trees.", example = "12")
    private Long parentCategoryId;

    @NotBlank
    @Schema(description = "Display name of the category.", example = "Home Decor")
    private String name;

    @NotBlank
    @Schema(description = "URL-safe slug for the category.", example = "home-decor")
    private String slug;

    @NotBlank
    @Schema(description = "Short description explaining the category.", example = "Decorative products for homes and offices.", nullable = true)
    private String description;

    @NotBlank
    @Schema(description = "Icon URL used when rendering the category in UI flows.", example = "https://cdn.nifilili.com/icons/home-decor.svg", nullable = true)
    private String iconUrl;

    @Schema(description = "Whether the category is active and selectable for businesses.", example = "true", defaultValue = "true")
    private boolean activeStatus;
}
