package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(
        name = "CreateBusinessRequest",
        description = "Payload used to create a new draft business during onboarding."
)
public class CreateBusinessRequest {

    @Schema(description = "Identifier of the selected business vertical.", example = "1")
    @NotNull
    private Long verticalId;

    @Schema(description = "Public-facing business name.", example = "Nifilili Crafts", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;

    @Schema(description = "Registered legal business name, if different from the display name.", example = "Nifilili Crafts Private Limited", nullable = true)
    private String legalName;

    @Schema(description = "Identifier of the municipality where the business is located.", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long municipalityId;

    @Schema(description = "Municipality ward number for the business address.", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer wardNumber;

    @Schema(description = "Tole or locality name for the address.", example = "Putalisadak", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String toleName;

    @Schema(description = "Primary address line for the business location.", example = "Bagbazar Main Road", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String addressField1;

    @Schema(description = "Secondary address line such as landmark or floor.", example = "2nd Floor, Opposite City Mall", nullable = true)
    private String addressField2;

    @Schema(description = "Postal code for the business address.", example = "44600")
    @NotBlank
    private String postalCode;

    @Schema(description = "Official website URL of the business.", example = "https://www.nifilili.com")
    @NotBlank
    private String website;

    @Schema(description = "URL of the business profile image.", example = "https://example.com/profile.jpg", nullable = true)
    private String profileImageUrl;

    @Schema(description = "URL of the business banner image.", example = "https://example.com/banner.jpg", nullable = true)
    private String bannerImageUrl;
}
