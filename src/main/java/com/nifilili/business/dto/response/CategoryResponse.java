package com.nifilili.business.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        name = "CategoryResponse",
        description = "Business category metadata used during onboarding and discovery."
)
public class CategoryResponse {

    @Schema(description = "Unique identifier of the category.", example = "11")
    private Long id;

    @Schema(description = "Identifier of the parent business vertical.", example = "1")
    private Long businessVerticalId;

    @Schema(description = "Optional parent category identifier for nested categories.", example = "10", nullable = true)
    private Long parentCategoryId;

    @Schema(description = "Display name of the category.", example = "Home Decor")
    private String name;

    @Schema(description = "URL-safe slug for the category.", example = "home-decor")
    private String slug;

    @Schema(description = "Short description of the category.", example = "Decorative products for homes and offices.", nullable = true)
    private String description;

    @Schema(description = "Icon URL representing the category.", example = "https://cdn.nifilili.com/icons/home-decor.svg", nullable = true)
    private String iconUrl;

    @Schema(description = "Whether the category is currently active.", example = "true")
    private boolean activeStatus;
}
