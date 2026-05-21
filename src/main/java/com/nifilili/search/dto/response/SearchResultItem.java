// FILE: com.nifilili.search.dto.response.SearchResultItem
package com.nifilili.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a single business result in the search response.
 * <p>
 * Designed for extensibility: new fields can be added to this DTO
 * without breaking existing API consumers, as JSON deserialization
 * ignores unknown properties by default.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single business search result item")
public class SearchResultItem {

    @Schema(description = "Unique business identifier", example = "123456789")
    private Long id;

    @Schema(description = "Business display name", example = "Pizza Palace")
    private String name;

    @Schema(description = "Business vertical/category identifier", example = "42")
    private Long verticalId;

    @Schema(description = "Average customer rating (0.0 to 5.0)", example = "4.5")
    private BigDecimal averageRating;

    @Schema(description = "Total number of customer reviews", example = "128")
    private Integer reviewCount;

    @Schema(description = "Whether the business has completed KYC verification", example = "true")
    private Boolean isKycVerified;

    @Schema(description = "Current business status", example = "PUBLISHED")
    private String status;

    @Schema(description = "Distance from user in kilometers (null if user location not provided)", example = "2.4")
    private BigDecimal distanceKm;

    @Schema(description = "Whether the business is currently open (null if businessHours not available)", example = "true")
    private Boolean isOpenNow;

    @Schema(description = "URL of the business profile image", example = "https://cdn.nifilili.com/img/pizza-palace.jpg")
    private String profileImageUrl;

    @Schema(description = "Formatted address string", example = "Ward 5, Kathmandu, Bagmati")
    private String formattedAddress;

    @Schema(description = "Municipality identifier for the business location", example = "101")
    private Long municipalityId;

    // EXTENSIBILITY: New fields can be added here without breaking consumers.
    // Example: private String categoryName;
    // Example: private List<String> tags;
}
