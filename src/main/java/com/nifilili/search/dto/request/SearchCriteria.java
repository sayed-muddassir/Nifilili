// FILE: com.nifilili.search.dto.request.SearchCriteria
package com.nifilili.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Contains the keyword-based search parameters for business search.
 * <p>
 * This class is designed for extensibility: new search parameters (e.g., tag lists,
 * category IDs) can be added as new fields without modifying the outer
 * {@link SearchRequest} structure, ensuring zero breaking changes for existing API consumers.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Keyword and parameter-based search criteria. Extensible — new search parameters can be added without breaking existing consumers.")
public class SearchCriteria {

    /** The free-text search query to match against business names and descriptions. */
    @Size(max = 200, message = "Search query must not exceed 200 characters")
    @Schema(
            description = "Free-text search query matched against business names and summaries via full-text search and trigram similarity",
            example = "pizza delivery",
            maxLength = 200
    )
    private String query;

    // EXTENSIBILITY: Add new search parameters here as needed.
    // Example: private List<Long> categoryIds;
    // Example: private List<String> tags;
    // Adding fields here does NOT change the outer SearchRequest shape.
}
