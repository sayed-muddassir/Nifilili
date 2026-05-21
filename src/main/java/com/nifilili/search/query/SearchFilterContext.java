// FILE: com.nifilili.search.query.SearchFilterContext
package com.nifilili.search.query;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Immutable value object that holds all filter values for a business search query.
 * <p>
 * Inspired by the Specification pattern, this context decouples filter values from
 * the request DTO layer and serves as the single transport object through the
 * service, builder, and repository layers.
 * </p>
 * <p>
 * Adding a new filter requires:
 * <ol>
 *   <li>Adding a field to this class</li>
 *   <li>Adding a {@code with*()} method to {@link BusinessSearchQueryBuilder}</li>
 * </ol>
 * No other classes need modification.
 * </p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilterContext {

    /** The free-text keyword search query. */
    private String keyword;

    /** Maximum distance in km from the user. */
    private BigDecimal distanceKm;

    /** User's latitude for geo calculations. */
    private BigDecimal userLat;

    /** User's longitude for geo calculations. */
    private BigDecimal userLng;

    /** Whether to filter for currently-open businesses (post-fetch). */
    private Boolean openNow;

    /** Whether to filter for KYC-verified businesses only. */
    private Boolean verifiedOnly;

    /** Minimum average rating threshold. */
    private BigDecimal minRating;

    /** Municipality ID filter. */
    private Long municipalityId;

    /** Vertical-specific attribute filters. */
    private Map<String, List<String>> verticalAttributes;

    // EXTENSIBILITY: Add new filter fields here.
    // Example: private Boolean hasParking;

    /**
     * Checks whether geo-distance filtering is applicable.
     * <p>
     * Geo filtering requires a valid distance, latitude, and longitude.
     * </p>
     *
     * @return true if all geo parameters are present and valid
     */
    public boolean hasGeoFilter() {
        return distanceKm != null && userLat != null && userLng != null;
    }

    /**
     * Checks whether a keyword search is requested.
     *
     * @return true if the keyword is non-null and non-blank
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.isBlank();
    }
}
