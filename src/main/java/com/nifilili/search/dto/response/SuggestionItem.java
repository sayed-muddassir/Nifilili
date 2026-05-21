// FILE: com.nifilili.search.dto.response.SuggestionItem
package com.nifilili.search.dto.response;

import com.nifilili.search.enums.SuggestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Represents a single auto-suggest item returned by the suggest endpoint.
 * <p>
 * Each suggestion has a type indicating its source (business, category, or location),
 * an optional ID for direct navigation, and display labels.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single auto-suggest item")
public class SuggestionItem {

    /** The type of this suggestion (BUSINESS, CATEGORY, or LOCATION). */
    @Schema(description = "Type of suggestion", example = "BUSINESS")
    private SuggestionType type;

    /** The entity ID (nullable for some types like LOCATION). */
    @Schema(description = "Entity ID (nullable for location suggestions)", example = "123456789")
    private Long id;

    /** Primary display label shown to the user. */
    @Schema(description = "Primary display label", example = "Pizza Palace")
    private String label;

    /** Secondary display label for additional context (e.g., address or category). */
    @Schema(description = "Secondary label for additional context", example = "Kathmandu, Ward 5")
    private String subLabel;
}
