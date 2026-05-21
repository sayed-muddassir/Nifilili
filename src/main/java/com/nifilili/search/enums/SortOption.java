// FILE: com.nifilili.search.enums.SortOption
package com.nifilili.search.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Defines the available sort options for business search results.
 * <p>
 * Each option maps to a {@link com.nifilili.search.strategy.SortStrategy}
 * implementation via the {@link com.nifilili.search.strategy.SortStrategyFactory}.
 * New sort options can be added by creating a new enum constant and a corresponding
 * strategy implementation — no changes to existing code required.
 * </p>
 */
@Schema(description = "Available sort options for business search results")
public enum SortOption {

    /** Sort by full-text search relevance score (default). */
    @Schema(description = "Sort by keyword relevance score")
    RELEVANCE,

    /** Sort by geographic distance from user's location (nearest first). */
    @Schema(description = "Sort by distance from user location, nearest first")
    DISTANCE,

    /** Sort by average rating (highest first). */
    @Schema(description = "Sort by average rating, highest first")
    RATING,

    /** Sort by popularity based on review count (most reviews first). */
    @Schema(description = "Sort by popularity (review count), most popular first")
    POPULARITY
}
