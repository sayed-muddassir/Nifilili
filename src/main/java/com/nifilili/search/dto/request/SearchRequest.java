// FILE: com.nifilili.search.dto.request.SearchRequest
package com.nifilili.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Top-level request object for the business search API.
 * <p>
 * This wrapper is intentionally thin, delegating search parameters to {@link SearchCriteria},
 * filter parameters to {@link SearchFilters}, and sort configuration to {@link SearchSort}.
 * This design ensures that adding new search parameters, filters, or sort options never
 * changes the outer request shape — only additive changes inside the nested objects.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Business search request. Extensible design — criteria, filters, and sort can evolve independently.")
public class SearchRequest {

    /** Search criteria containing keyword and future search parameters. */
    @Valid
    @Schema(description = "Search criteria containing keyword and search parameters")
    @NotNull(message = "Search criteria must not be null")
    private SearchCriteria criteria;

    /** Optional filters to narrow down results. */
    @Valid
    @Schema(description = "Optional filters to narrow search results")
    private SearchFilters filters;

    /** Sort configuration. Defaults to relevance sort. */
    @Valid
    @Schema(description = "Sort configuration")
    private SearchSort sort;

    /** Zero-based page number. */
    @Min(value = 0, message = "Page number must be zero or positive")
    @Schema(description = "Zero-based page number", example = "0", defaultValue = "0")
    @NotNull
    @Builder.Default
    private int page = 0;

    /** Page size. Minimum 1, maximum 100, default 20. */
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    @Schema(description = "Number of results per page", example = "20", defaultValue = "20")
    @NotNull
    @Builder.Default
    private int size = 20;
}
