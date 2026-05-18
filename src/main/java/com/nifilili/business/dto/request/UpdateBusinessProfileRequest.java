package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Schema(description = "Registered legal name of the business.", example = "Nifilili Crafts Private Limited", nullable = true)
    private String legalName;

    @Schema(description = "Secondary address line such as landmark, suite, or floor.", example = "2nd Floor, Opposite City Mall", nullable = true)
    private String addressField2;

    @Schema(description = "Contact metadata such as phone, email, or social handles.", example = "{\"phone\":\"+977-9800000000\",\"email\":\"hello@nifilili.com\"}", nullable = true)
    private Map<String, Object> contacts;

    @Schema(description = "Business operating hours keyed by day or schedule block.", example = "{\"monday\":\"09:00-18:00\",\"tuesday\":\"09:00-18:00\"}", nullable = true)
    private Map<String, Object> businessHours;

    @Schema(description = "Latitude coordinate of the business location.", example = "27.7172")
    private BigDecimal latitude;

    @Schema(description = "Longitude coordinate of the business location.", example = "85.3240")
    private BigDecimal longitude;
}
