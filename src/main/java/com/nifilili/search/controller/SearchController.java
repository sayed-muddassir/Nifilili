// FILE: com.nifilili.search.controller.SearchController
package com.nifilili.search.controller;

import com.nifilili.search.dto.request.AutoSuggestRequest;
import com.nifilili.search.dto.request.SearchRequest;
import com.nifilili.search.dto.response.ApiResponse;
import com.nifilili.search.dto.response.AutoSuggestResponse;
import com.nifilili.search.dto.response.SearchResponse;
import com.nifilili.search.service.AutoSuggestService;
import com.nifilili.search.service.BusinessSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST controller for the Search and Discovery module.
 * <p>
 * Provides endpoints for full business search and type-ahead auto-suggest.
 * All responses are wrapped in {@link ApiResponse} for a consistent API envelope.
 * </p>
 * <p>
 * This controller contains no business logic — it only validates requests
 * and delegates to the appropriate service. Exception handling is managed
 * by the global {@code @ControllerAdvice}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/public/search")
@RequiredArgsConstructor
@Tag(name = "Search & Discovery", description = "Business search and auto-suggest endpoints")
public class SearchController {

    private final BusinessSearchService businessSearchService;
    private final AutoSuggestService autoSuggestService;

    /**
     * Searches for businesses based on keyword, filters, and sorting criteria.
     *
     * @param request the validated search request body
     * @return a paginated response with matching business results
     */
    @PostMapping("/businesses")
    @Operation(
            summary = "Search businesses",
            description = "Full-text search with filters, geo-distance, and sorting. " +
                    "Supports keyword matching via PostgreSQL FTS and trigram similarity."
    )
    public ResponseEntity<ApiResponse<SearchResponse>> searchBusinesses(
            @Valid @RequestBody SearchRequest request
    ) {
        SearchResponse response = businessSearchService.search(request);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Search completed successfully")
        );
    }

    /**
     * Provides type-ahead auto-suggest results for a partial query.
     *
     * @param query   the partial search query (required)
     * @param userLat optional user latitude for proximity boosting
     * @param userLng optional user longitude for proximity boosting
     * @param limit   maximum number of suggestions (default 8, max 20)
     * @return a list of suggestion items ordered by relevance
     */
    @GetMapping("/suggest")
    @Operation(
            summary = "Auto-suggest",
            description = "Returns type-ahead suggestions for businesses and categories " +
                    "based on trigram similarity and ILIKE matching."
    )
    public ResponseEntity<ApiResponse<AutoSuggestResponse>> autoSuggest(
            @Parameter(description = "Partial search query", required = true, example = "piz")
            @RequestParam("query")
            @NotBlank(message = "Query must not be blank")
            @Size(min = 1, max = 100, message = "Query must be between 1 and 100 characters")
            String query,

            @Parameter(description = "User latitude for proximity boosting", example = "27.7172")
            @RequestParam(value = "userLat", required = false)
            @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
            @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
            BigDecimal userLat,

            @Parameter(description = "User longitude for proximity boosting", example = "85.3240")
            @RequestParam(value = "userLng", required = false)
            @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
            @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
            BigDecimal userLng,

            @Parameter(description = "Maximum number of suggestions", example = "8")
            @RequestParam(value = "limit", required = false, defaultValue = "8")
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 20, message = "Limit must not exceed 20")
            int limit
    ) {
        AutoSuggestRequest request = AutoSuggestRequest.builder()
                .query(query)
                .userLat(userLat)
                .userLng(userLng)
                .limit(limit)
                .build();

        AutoSuggestResponse response = autoSuggestService.suggest(request);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Suggestions retrieved successfully")
        );
    }
}
