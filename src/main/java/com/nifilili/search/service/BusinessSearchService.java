// FILE: com.nifilili.search.service.BusinessSearchService
package com.nifilili.search.service;

import com.nifilili.search.dto.request.SearchFilters;
import com.nifilili.search.dto.request.SearchRequest;
import com.nifilili.search.dto.request.SearchSort;
import com.nifilili.search.dto.response.SearchResponse;
import com.nifilili.search.dto.response.SearchResultItem;
import com.nifilili.search.enums.SortOption;
import com.nifilili.search.mapper.BusinessSearchResultMapper;
import com.nifilili.search.query.BusinessSearchQueryBuilder;
import com.nifilili.search.query.SearchFilterContext;
import com.nifilili.search.query.SearchQueryResult;
import com.nifilili.search.repository.BusinessSearchRepository;
import com.nifilili.search.strategy.SortStrategy;
import com.nifilili.search.strategy.SortStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Core orchestration service for business search operations.
 * <p>
 * Implements the full search pipeline:
 * <ol>
 *   <li>Map request DTOs → {@link SearchFilterContext}</li>
 *   <li>Resolve sort strategy via {@link SortStrategyFactory}</li>
 *   <li>Build native SQL query via {@link BusinessSearchQueryBuilder}</li>
 *   <li>Execute query via {@link BusinessSearchRepository}</li>
 *   <li>Map raw results via {@link BusinessSearchResultMapper}</li>
 *   <li>Apply post-fetch filters (e.g., openNow)</li>
 *   <li>Build and return {@link SearchResponse}</li>
 * </ol>
 * </p>
 * <p>
 * This service contains no SQL, no column knowledge, and no direct DB access.
 * Each concern is delegated to its specialized component.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessSearchService {

    private final SortStrategyFactory sortStrategyFactory;
    private final BusinessSearchRepository searchRepository;
    private final BusinessSearchResultMapper resultMapper;

    /**
     * Executes a full business search based on the provided request.
     *
     * @param request the validated search request containing criteria, filters, sort, and pagination
     * @return a paginated {@link SearchResponse} with matched business results
     */
    public SearchResponse search(SearchRequest request) {
        log.debug("Executing business search: page={}, size={}", request.getPage(), request.getSize());

        // Step 1: Build filter context from request
        SearchFilterContext filterContext = buildFilterContext(request);

        // Step 2: Resolve sort strategy
        SortStrategy sortStrategy = resolveSortStrategy(request.getSort());

        // Step 3: Build the SQL query
        SearchQueryResult queryResult = buildSearchQuery(
                filterContext, sortStrategy, request.getPage(), request.getSize()
        );

        // Step 4: Execute queries
        List<Object[]> rawResults = searchRepository.executeSearch(queryResult);
        long totalCount = searchRepository.executeCount(queryResult);

        // Step 5: Map results and apply post-fetch filters
        List<SearchResultItem> results = resultMapper.mapResults(rawResults, filterContext);

        // Step 6: Build paginated response
        return buildSearchResponse(results, totalCount, request.getPage(), request.getSize());
    }

    /**
     * Builds a {@link SearchFilterContext} from the incoming request.
     * <p>
     * Extracts keyword from criteria and all filter values from the filters DTO,
     * consolidating them into a single context object for downstream processing.
     * </p>
     *
     * @param request the search request
     * @return a populated SearchFilterContext
     */
    private SearchFilterContext buildFilterContext(SearchRequest request) {
        SearchFilterContext.SearchFilterContextBuilder builder = SearchFilterContext.builder();

        // Extract keyword from criteria
        if (request.getCriteria() != null) {
            builder.keyword(request.getCriteria().getQuery());
        }

        // Extract filters
        SearchFilters filters = request.getFilters();
        if (filters != null) {
            builder.distanceKm(filters.getDistanceKm())
                    .userLat(filters.getUserLat())
                    .userLng(filters.getUserLng())
//                    .openNow(filters.getOpenNow())
                    .verifiedOnly(filters.getVerifiedOnly())
                    .minRating(filters.getMinRating())
                    .municipalityId(filters.getMunicipalityId());
//                    .verticalAttributes(filters.getVerticalAttributes());
        }

        return builder.build();
    }

    /**
     * Resolves the appropriate sort strategy from the request's sort configuration.
     *
     * @param searchSort the sort configuration from the request (nullable)
     * @return the resolved SortStrategy, defaults to RELEVANCE
     */
    private SortStrategy resolveSortStrategy(SearchSort searchSort) {
        SortOption sortOption = (searchSort != null) ? searchSort.getSortBy() : SortOption.RELEVANCE;
        return sortStrategyFactory.resolve(sortOption);
    }

    /**
     * Constructs the search query using the fluent builder.
     * <p>
     * Each filter is applied independently via its own {@code with*()} method.
     * The order of method calls does not matter.
     * </p>
     *
     * @param context      the consolidated filter context
     * @param sortStrategy the resolved sort strategy
     * @param page         the zero-based page number
     * @param size         the page size
     * @return the constructed SearchQueryResult
     */
    private SearchQueryResult buildSearchQuery(
            SearchFilterContext context,
            SortStrategy sortStrategy,
            int page,
            int size
    ) {
        return new BusinessSearchQueryBuilder()
                .withKeyword(context)
                .withGeoFilter(context)
                .withLocationFilter(context)
                .withVerificationFilter(context)
                .withRatingFilter(context)
                .withVerticalAttributeFilter(context)
                .withSort(sortStrategy)
                .withPagination(page, size)
                .build();
    }

    /**
     * Builds the final paginated search response.
     *
     * @param results    the mapped result items
     * @param totalCount the total number of matching results
     * @param page       the current page number
     * @param size       the page size
     * @return a complete SearchResponse
     */
    private SearchResponse buildSearchResponse(
            List<SearchResultItem> results,
            long totalCount,
            int page,
            int size
    ) {
        boolean hasNext = ((long) (page + 1) * size) < totalCount;

        return SearchResponse.builder()
                .total(totalCount)
                .page(page)
                .size(size)
                .hasNext(hasNext)
                .results(results)
                .build();
    }
}
