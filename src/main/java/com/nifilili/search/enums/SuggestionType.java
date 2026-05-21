// FILE: com.nifilili.search.enums.SuggestionType
package com.nifilili.search.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Categorizes auto-suggest results by their source type.
 * <p>
 * Used in the auto-suggest response to indicate whether a suggestion
 * refers to a specific business listing, a category/vertical, or a
 * geographic location.
 * </p>
 */
@Schema(description = "Type of auto-suggest result")
public enum SuggestionType {

    /** Suggestion refers to a specific business listing. */
    @Schema(description = "A specific business listing")
    BUSINESS,

    /** Suggestion refers to a business category or vertical. */
    @Schema(description = "A business category or vertical")
    CATEGORY,

    /** Suggestion refers to a geographic location (municipality, ward, etc.). */
    @Schema(description = "A geographic location")
    LOCATION
}
