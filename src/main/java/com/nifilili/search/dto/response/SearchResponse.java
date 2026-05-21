// FILE: com.nifilili.search.dto.response.SearchResponse
package com.nifilili.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Paginated response wrapper for business search results.
 * <p>
 * Contains the result list along with pagination metadata.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated business search response")
public class SearchResponse {

    /** Total number of matching results across all pages. */
    @Schema(description = "Total number of matching results", example = "142")
    private long total;

    /** Current zero-based page number. */
    @Schema(description = "Current page number (zero-based)", example = "0")
    private int page;

    /** Number of results per page. */
    @Schema(description = "Page size", example = "20")
    private int size;

    /** Whether there are more pages after the current one. */
    @Schema(description = "True if there are more pages available", example = "true")
    private boolean hasNext;

    /** The list of business results for the current page. */
    @Schema(description = "Search result items for the current page")
    private List<SearchResultItem> results;
}
