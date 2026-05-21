// FILE: com.nifilili.search.dto.request.SearchSort
package com.nifilili.search.dto.request;

import com.nifilili.search.enums.SortOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Defines the sort configuration for business search results.
 * <p>
 * Currently supports a single primary sort option. The structure includes
 * a {@code secondarySorts} list for future multi-sort support without
 * breaking the existing API contract.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sort configuration. Supports a primary sort option with future extensibility for secondary sorts.")
public class SearchSort {

    /** The primary sort option. Defaults to RELEVANCE if not specified. */
    @Schema(description = "Primary sort option", example = "RELEVANCE", defaultValue = "RELEVANCE")
    @Builder.Default
    private SortOption sortBy = SortOption.RELEVANCE;

    /**
     * Reserved for future multi-sort support.
     * Secondary sort options applied after the primary sort.
     */
    @Schema(description = "Secondary sort options for tie-breaking (reserved for future use)")
    private List<SortOption> secondarySorts;
}
