// FILE: com.nifilili.search.strategy.SortStrategyFactory
package com.nifilili.search.strategy;

import com.nifilili.search.enums.SortOption;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory that resolves {@link SortStrategy} implementations from {@link SortOption} enum values.
 * <p>
 * Implements the Factory design pattern. Strategies are registered at construction time
 * in an {@link EnumMap} for O(1) lookup. To add a new sort option:
 * <ol>
 *   <li>Create a new {@link SortStrategy} implementation</li>
 *   <li>Add a new constant to {@link SortOption}</li>
 *   <li>Register the mapping in the constructor of this class</li>
 * </ol>
 * No existing code needs modification — only additive changes.
 * </p>
 */
@Component
public class SortStrategyFactory {

    /** Map of sort option enum constants to their corresponding strategy instances. */
    private final Map<SortOption, SortStrategy> strategies;

    /**
     * Initializes the factory with all registered sort strategy mappings.
     */
    public SortStrategyFactory() {
        this.strategies = new EnumMap<>(SortOption.class);
        this.strategies.put(SortOption.RELEVANCE, new RelevanceSortStrategy());
        this.strategies.put(SortOption.DISTANCE, new DistanceSortStrategy());
        this.strategies.put(SortOption.RATING, new RatingSortStrategy());
        this.strategies.put(SortOption.POPULARITY, new PopularitySortStrategy());
    }

    /**
     * Resolves the appropriate {@link SortStrategy} for the given sort option.
     * <p>
     * Falls back to {@link RelevanceSortStrategy} if the option is null or not registered.
     * </p>
     *
     * @param sortOption the sort option to resolve; may be null
     * @return the corresponding {@link SortStrategy}, never null
     */
    public SortStrategy resolve(SortOption sortOption) {
        if (sortOption == null) {
            return strategies.get(SortOption.RELEVANCE);
        }
        return strategies.getOrDefault(sortOption, strategies.get(SortOption.RELEVANCE));
    }
}
