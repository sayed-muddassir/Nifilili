// FILE: com.nifilili.search.dto.request.SearchFilters
package com.nifilili.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Contains all filter parameters for business search.
 * <p>
 * Designed for extensibility: adding a new filter requires only adding a new field here
 * and a corresponding {@code with*()} method in
 * {@link com.nifilili.search.query.BusinessSearchQueryBuilder}.
 * No changes to existing filter fields or the outer request shape.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search filters. All filters are optional and additive (AND logic). New filters can be added without breaking the API contract.")
public class SearchFilters {

    /** Maximum distance in kilometers from the user's location. Requires userLat and userLng. */
    @DecimalMin(value = "0.1", message = "Distance must be at least 0.1 km")
    @DecimalMax(value = "500.0", message = "Distance must not exceed 500 km")
    @Schema(description = "Maximum distance in km from user location. Requires userLat and userLng to be set.", example = "10.0")
    private BigDecimal distanceKm;

//    /** When true, only return businesses currently open. Evaluated in-memory against businessHours JSONB. */
//    @Schema(description = "When true, only return businesses that are currently open", example = "false")
//    private Boolean openNow; TODO

    /** When true, only return KYC-verified businesses. */
    @Schema(description = "When true, only return KYC-verified businesses", example = "false")
    private Boolean verifiedOnly;

    /** Minimum average rating threshold (inclusive). */
    @DecimalMin(value = "0.0", message = "Minimum rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Minimum rating must not exceed 5.0")
    @Schema(description = "Minimum average rating (inclusive)", example = "4.0")
    private BigDecimal minRating;

    /** Filter by municipality ID. */
    @Schema(description = "Filter by municipality ID", example = "101")
    private Long municipalityId;

    /** User's latitude for distance-based filtering and sorting. */
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Schema(description = "User's latitude for distance calculations", example = "27.7172")
    private BigDecimal userLat;

    /** User's longitude for distance-based filtering and sorting. */
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Schema(description = "User's longitude for distance calculations", example = "85.3240")
    private BigDecimal userLng;

//    /**
//     * Vertical-specific attribute filters as key-value pairs.
//     * <p>Example: {"cuisine": ["italian", "chinese"], "priceRange": ["$$"]}</p>
//     */
//    @Schema(
//            description = "Vertical-specific attribute filters. Keys are attribute names, values are lists of acceptable values.",
//            example = "{\"cuisine\": [\"italian\", \"chinese\"], \"priceRange\": [\"$$\"]}"
//    )
//    private Map<String, List<String>> verticalAttributes; TODO

    // EXTENSIBILITY: Add new filter fields here.
    // Example: private Boolean hasParking;
    // Then add a corresponding with*() method in BusinessSearchQueryBuilder.
}
