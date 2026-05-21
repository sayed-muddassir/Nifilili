// FILE: com.nifilili.search.strategy.PopularitySortStrategy
package com.nifilili.search.strategy;

/**
 * Sort strategy that orders results by popularity (review count).
 * <p>
 * Businesses with the most reviews appear first. NULL review counts are
 * treated as zero and sorted last. This provides a proxy for business
 * popularity and user engagement.
 * </p>
 * <p>
 * Design Pattern: Strategy (concrete implementation).
 * </p>
 */
public class PopularitySortStrategy implements SortStrategy {

    /** SQL fragment for ordering by review count descending, nulls last. */
    private static final String POPULARITY_ORDER = "b.review_count DESC NULLS LAST";

    /**
     * Returns the SQL ORDER BY clause fragment for popularity-based sorting.
     *
     * @return SQL fragment ordering by review_count descending
     */
    @Override
    public String toOrderByClause() {
        return POPULARITY_ORDER;
    }
}
