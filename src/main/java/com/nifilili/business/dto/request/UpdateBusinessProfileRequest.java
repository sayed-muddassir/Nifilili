package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Schema(
        name = "UpdateBusinessProfileRequest",
        description = "Payload used to update the core profile metadata of an existing business."
)
public class UpdateBusinessProfileRequest {

    @Schema(description = "Short summary describing the business, offerings, or value proposition.", example = "Handcrafted home decor and locally sourced gift products.", nullable = true)
    private String businessSummary;

    @Schema(description = "Identifier of the selected business vertical.", example = "1")
    private Long verticalId;

    @Schema(description = "Public-facing business name.", example = "Nifilili Crafts", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Registered legal business name, if different from the display name.", example = "Nifilili Crafts Private Limited", nullable = true)
    private String legalName;

    @Schema(description = "Identifier of the municipality where the business is located.", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long municipalityId;

    @Schema(description = "Municipality ward number for the business address.", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer wardNumber;

    @Schema(description = "Tole or locality name for the address.", example = "Putalisadak", requiredMode = Schema.RequiredMode.REQUIRED)
    private String toleName;

    @Schema(description = "Primary address line for the business location.", example = "Bagbazar Main Road", requiredMode = Schema.RequiredMode.REQUIRED)
    private String addressField1;

    @Schema(description = "Secondary address line such as landmark or floor.", example = "2nd Floor, Opposite City Mall", nullable = true)
    private String addressField2;

    @Schema(description = "Postal code for the business address.", example = "44600", nullable = true)
    private String postalCode;

    @Schema(description = "Official website URL of the business.", example = "https://www.nifilili.com", nullable = true)
    private String website;

    @Schema(description = "URL of the business profile image.", example = "https://example.com/profile.jpg", nullable = true)
    private String profileImageUrl;

    @Schema(description = "URL of the business banner image.", example = "https://example.com/banner.jpg", nullable = true)
    private String bannerImageUrl;

    @Schema(description = "Contact metadata such as phone, email, or social handles.", example = "{\"phone\":\"+977-9800000000\",\"email\":\"hello@nifilili.com\"}", nullable = true)
    private Map<String, Object> contacts;

    @Schema(description = "Business operating hours keyed by day or schedule block.", example = "{\"monday\":\"09:00-18:00\",\"tuesday\":\"09:00-18:00\"}", nullable = true)
    private Map<String, Object> businessHours;

    @Schema(description = "Latitude coordinate of the business location.", example = "27.7172")
    private BigDecimal latitude;

    @Schema(description = "Longitude coordinate of the business location.", example = "85.3240")
    private BigDecimal longitude;
}
