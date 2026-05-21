// FILE: com.nifilili.search.dto.request.AutoSuggestRequest
package com.nifilili.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request parameters for the auto-suggest endpoint.
 * <p>
 * Accepts a partial query string and optional user location to return
 * ranked suggestions of businesses, categories, and locations.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Auto-suggest request for type-ahead search functionality")
public class AutoSuggestRequest {

    /** The partial search query entered by the user. */
    @NotBlank(message = "Query must not be blank")
    @Size(min = 1, max = 100, message = "Query must be between 1 and 100 characters")
    @Schema(description = "Partial search query for type-ahead suggestions", example = "piz", requiredMode = Schema.RequiredMode.REQUIRED)
    private String query;

    /** User's current latitude for distance-boosted suggestions. */
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Schema(description = "User's latitude for proximity-boosted suggestions", example = "27.7172")
    private BigDecimal userLat;

    /** User's current longitude for distance-boosted suggestions. */
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Schema(description = "User's longitude for proximity-boosted suggestions", example = "85.3240")
    private BigDecimal userLng;

    /** Maximum number of suggestions to return. */
    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 20, message = "Limit must not exceed 20")
    @Schema(description = "Maximum number of suggestions to return", example = "8", defaultValue = "8")
    @Builder.Default
    private int limit = 8;
}
