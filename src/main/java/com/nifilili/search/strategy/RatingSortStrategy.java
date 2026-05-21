// FILE: com.nifilili.search.strategy.RatingSortStrategy
package com.nifilili.search.strategy;

/**
 * Sort strategy that orders results by average customer rating.
 * <p>
 * Businesses with the highest ratings appear first. NULL ratings are sorted
 * last to ensure rated businesses always appear before unrated ones.
 * </p>
 * <p>
 * Design Pattern: Strategy (concrete implementation).
 * </p>
 */
public class RatingSortStrategy implements SortStrategy {

    /** SQL fragment for ordering by average rating descending, nulls last. */
    private static final String RATING_ORDER = "b.average_rating DESC NULLS LAST";

    /**
     * Returns the SQL ORDER BY clause fragment for rating-based sorting.
     *
     * @return SQL fragment ordering by average_rating descending
     */
    @Override
    public String toOrderByClause() {
        return RATING_ORDER;
    }
}
