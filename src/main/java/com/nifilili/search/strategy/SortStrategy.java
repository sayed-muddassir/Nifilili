// FILE: com.nifilili.search.strategy.SortStrategy
package com.nifilili.search.strategy;

/**
 * Strategy interface for generating SQL ORDER BY clauses.
 * <p>
 * Implements the Strategy design pattern to decouple sort logic from the
 * query builder and repository layers. Each sort option has its own strategy
 * implementation, making the system open for extension (new sort options)
 * and closed for modification (existing strategies remain unchanged).
 * </p>
 * <p>
 * To add a new sort option:
 * <ol>
 *   <li>Create a new implementation of this interface</li>
 *   <li>Add the new enum constant to {@link com.nifilili.search.enums.SortOption}</li>
 *   <li>Register the mapping in {@link SortStrategyFactory}</li>
 * </ol>
 * </p>
 */
public interface SortStrategy {

    /**
     * Generates the SQL ORDER BY clause fragment for this sort strategy.
     * <p>
     * The returned string should NOT include the "ORDER BY" keyword itself,
     * only the column expression and direction (e.g., "b.average_rating DESC NULLS LAST").
     * </p>
     *
     * @return a valid SQL ORDER BY clause fragment
     */
    String toOrderByClause();
}
