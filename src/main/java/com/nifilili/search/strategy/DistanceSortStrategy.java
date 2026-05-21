// FILE: com.nifilili.search.strategy.DistanceSortStrategy
package com.nifilili.search.strategy;

/**
 * Sort strategy that orders results by geographic distance from the user.
 * <p>
 * Uses the Haversine-computed {@code distance_km} column to order results
 * from nearest to farthest. When user coordinates are not provided, this
 * strategy gracefully falls back to a null-safe ordering.
 * </p>
 * <p>
 * Design Pattern: Strategy (concrete implementation).
 * </p>
 */
public class DistanceSortStrategy implements SortStrategy {

    /** SQL fragment for ordering by computed Haversine distance ascending. */
    private static final String DISTANCE_ORDER = "distance_km ASC NULLS LAST";

    /**
     * Returns the SQL ORDER BY clause fragment for distance-based sorting.
     *
     * @return SQL fragment ordering by distance_km ascending
     */
    @Override
    public String toOrderByClause() {
        return DISTANCE_ORDER;
    }
}
