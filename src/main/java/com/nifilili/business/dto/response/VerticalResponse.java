package com.nifilili.business.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        name = "VerticalResponse",
        description = "Business vertical metadata exposed to onboarding and admin clients."
)
public class VerticalResponse {

    @Schema(description = "Unique identifier of the vertical.", example = "1")
    private Long id;

    @Schema(description = "Display name of the vertical.", example = "Retail")
    private String name;

    @Schema(description = "URL-safe slug for the vertical.", example = "retail")
    private String slug;

    @Schema(description = "Short description of the vertical.", example = "Retail businesses including storefronts and online sellers.", nullable = true)
    private String description;

    @Schema(description = "Icon URL for the vertical.", example = "https://cdn.nifilili.com/icons/retail.svg", nullable = true)
    private String iconUrl;

    @Schema(description = "Whether the vertical is currently active.", example = "true")
    private boolean isActive;
}
