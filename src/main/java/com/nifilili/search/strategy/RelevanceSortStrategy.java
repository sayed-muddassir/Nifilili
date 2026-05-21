// FILE: com.nifilili.search.strategy.RelevanceSortStrategy
package com.nifilili.search.strategy;

/**
 * Sort strategy that orders results by full-text search relevance score.
 * <p>
 * Uses the PostgreSQL {@code ts_rank} function score to order results
 * by how well they match the search query. Results with higher relevance
 * appear first. This is the default sort strategy when no specific sort
 * option is requested.
 * </p>
 * <p>
 * Design Pattern: Strategy (concrete implementation).
 * </p>
 */
public class RelevanceSortStrategy implements SortStrategy {

    /** SQL fragment for ordering by full-text search rank descending. */
    private static final String RELEVANCE_ORDER = "search_rank DESC NULLS LAST";

    /**
     * Returns the SQL ORDER BY clause fragment for relevance-based sorting.
     *
     * @return SQL fragment ordering by ts_rank score descending
     */
    @Override
    public String toOrderByClause() {
        return RELEVANCE_ORDER;
    }
}
