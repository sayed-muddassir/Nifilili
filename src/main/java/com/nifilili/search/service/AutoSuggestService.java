// FILE: com.nifilili.search.service.AutoSuggestService
package com.nifilili.search.service;

import com.nifilili.core.exception.SearchExecutionException;
import com.nifilili.search.dto.request.AutoSuggestRequest;
import com.nifilili.search.dto.response.AutoSuggestResponse;
import com.nifilili.search.dto.response.SuggestionItem;
import com.nifilili.search.enums.SuggestionType;
import com.nifilili.search.repository.AutoSuggestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Service for providing type-ahead auto-suggest functionality.
 * <p>
 * Runs two parallel queries using {@link CompletableFuture}:
 * <ol>
 *   <li>Business name suggestions via trigram similarity (up to 5 results)</li>
 *   <li>Category/vertical name suggestions via ILIKE (up to 3 results)</li>
 * </ol>
 * Results are merged with business suggestions appearing first, followed by
 * category suggestions, providing a unified suggestion list.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoSuggestService {

    private final AutoSuggestRepository autoSuggestRepository;

    /** Maximum time in seconds to wait for suggestion queries. */
    private static final long SUGGEST_TIMEOUT_SECONDS = 5;

    /**
     * Generates auto-suggest results for the given request.
     * <p>
     * Executes business and category suggestion queries in parallel,
     * then merges results with business suggestions first.
     * </p>
     *
     * @param request the auto-suggest request with query and optional location
     * @return an AutoSuggestResponse containing merged suggestions
     * @throws SearchExecutionException if the suggestion queries fail or timeout
     */
    public AutoSuggestResponse suggest(AutoSuggestRequest request) {
        log.debug("Executing auto-suggest for query: '{}'", request.getQuery());

        String query = request.getQuery().trim();
        int limit = request.getLimit();

        // Launch parallel queries
        CompletableFuture<List<SuggestionItem>> businessFuture =
                CompletableFuture.supplyAsync(() -> fetchBusinessSuggestions(query, request));

        CompletableFuture<List<SuggestionItem>> categoryFuture =
                CompletableFuture.supplyAsync(() -> fetchCategorySuggestions(query, limit));

        // Wait for both and merge
        try {
            CompletableFuture.allOf(businessFuture, categoryFuture)
                    .get(SUGGEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            List<SuggestionItem> merged = mergeResults(
                    businessFuture.get(),
                    categoryFuture.get()
            );

            return AutoSuggestResponse.builder()
                    .suggestions(merged)
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SearchExecutionException("Auto-suggest was interrupted", e);
        } catch (ExecutionException e) {
            throw new SearchExecutionException(
                    "Auto-suggest query failed: " + e.getCause().getMessage(), e.getCause()
            );
        } catch (TimeoutException e) {
            throw new SearchExecutionException("Auto-suggest timed out after " + SUGGEST_TIMEOUT_SECONDS + " seconds", e);
        }
    }

    /**
     * Fetches business name suggestions from the repository.
     *
     * @param query   the trimmed search query
     * @param request the original request for user location data
     * @return list of business suggestion items
     */
    private List<SuggestionItem> fetchBusinessSuggestions(
            String query,
            AutoSuggestRequest request
    ) {
        List<Object[]> rows = autoSuggestRepository.findBusinessSuggestions(
                query, request.getUserLat(), request.getUserLng(), request.getLimit()
        );
        return mapBusinessSuggestions(rows);
    }

    /**
     * Fetches category/vertical name suggestions from the repository.
     *
     * @param query the trimmed search query
     * @param limit the maximum number of results
     * @return list of category suggestion items
     */
    private List<SuggestionItem> fetchCategorySuggestions(String query, int limit) {
        List<Object[]> rows = autoSuggestRepository.findCategorySuggestions(query, limit);
        return mapCategorySuggestions(rows);
    }

    /**
     * Maps raw business suggestion rows to SuggestionItem DTOs.
     * <p>
     * Row format: [id, name, toleName, municipalityId, simScore, distanceKm]
     * </p>
     *
     * @param rows the raw query result rows
     * @return mapped suggestion items
     */
    private List<SuggestionItem> mapBusinessSuggestions(List<Object[]> rows) {
        List<SuggestionItem> items = new ArrayList<>();
        for (Object[] row : rows) {
            Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
            String name = row[1] != null ? row[1].toString() : null;
            String toleName = row[2] != null ? row[2].toString() : null;

            items.add(SuggestionItem.builder()
                    .type(SuggestionType.BUSINESS)
                    .id(id)
                    .label(name)
                    .subLabel(toleName)
                    .build());
        }
        return items;
    }

    /**
     * Maps raw category suggestion rows to SuggestionItem DTOs.
     * <p>
     * Row format: [id, name]
     * </p>
     *
     * @param rows the raw query result rows
     * @return mapped suggestion items
     */
    private List<SuggestionItem> mapCategorySuggestions(List<Object[]> rows) {
        List<SuggestionItem> items = new ArrayList<>();
        for (Object[] row : rows) {
            Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
            String name = row[1] != null ? row[1].toString() : null;

            items.add(SuggestionItem.builder()
                    .type(SuggestionType.CATEGORY)
                    .id(id)
                    .label(name)
                    .subLabel(null)
                    .build());
        }
        return items;
    }

    /**
     * Merges business and category suggestions, with business suggestions first.
     *
     * @param businessSuggestions the business suggestion items
     * @param categorySuggestions the category suggestion items
     * @return a merged list with business suggestions followed by category suggestions
     */
    private List<SuggestionItem> mergeResults(
            List<SuggestionItem> businessSuggestions,
            List<SuggestionItem> categorySuggestions
    ) {
        List<SuggestionItem> merged = new ArrayList<>(businessSuggestions.size() + categorySuggestions.size());
        merged.addAll(businessSuggestions);
        merged.addAll(categorySuggestions);
        return merged;
    }
}
