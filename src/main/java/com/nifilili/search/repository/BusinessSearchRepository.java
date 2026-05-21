// FILE: com.nifilili.search.repository.BusinessSearchRepository
package com.nifilili.search.repository;

import com.nifilili.core.exception.SearchExecutionException;
import com.nifilili.search.query.SearchQueryResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Repository for executing business search queries using native SQL via {@link EntityManager}.
 * <p>
 * This repository is intentionally thin — it only executes pre-constructed queries
 * and returns raw results. All query construction logic resides in
 * {@link com.nifilili.search.query.BusinessSearchQueryBuilder}, and all result
 * mapping logic resides in {@link com.nifilili.search.mapper.BusinessSearchResultMapper}.
 * </p>
 * <p>
 * This design ensures the repository has a single responsibility: query execution.
 * It is completely unaware of business logic, filtering rules, or DTO structures.
 * </p>
 */
@Repository
public class BusinessSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Executes the search data query and returns raw result rows.
     * <p>
     * Each row is returned as an {@code Object[]} whose column positions
     * correspond to the SELECT clause defined in
     * {@link com.nifilili.search.query.BusinessSearchQueryBuilder}.
     * </p>
     *
     * @param queryResult the pre-built query containing SQL and parameters
     * @return a list of raw result rows as Object arrays
     * @throws SearchExecutionException if the query fails to execute
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> executeSearch(SearchQueryResult queryResult) {
        try {
            Query query = entityManager.createNativeQuery(queryResult.getSql());
            bindParameters(query, queryResult.getParameters());
            return query.getResultList();
        } catch (Exception e) {
            throw new SearchExecutionException(
                    "Failed to execute business search query: " + e.getMessage(), e
            );
        }
    }

    /**
     * Executes the count query and returns the total number of matching results.
     *
     * @param queryResult the pre-built query containing the count SQL and parameters
     * @return the total count of matching results
     * @throws SearchExecutionException if the count query fails to execute
     */
    public long executeCount(SearchQueryResult queryResult) {
        try {
            Query query = entityManager.createNativeQuery(queryResult.getCountSql());
            // Bind all parameters except pagination-specific ones
            Map<String, Object> params = queryResult.getParameters();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                if (!"limit".equals(key) && !"offset".equals(key)) {
                    query.setParameter(key, entry.getValue());
                }
            }
            Number count = (Number) query.getSingleResult();
            return count.longValue();
        } catch (Exception e) {
            throw new SearchExecutionException(
                    "Failed to execute search count query: " + e.getMessage(), e
            );
        }
    }

    /**
     * Binds all named parameters to the query.
     *
     * @param query  the JPA native query
     * @param params the parameter map to bind
     */
    private void bindParameters(Query query, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
    }
}
