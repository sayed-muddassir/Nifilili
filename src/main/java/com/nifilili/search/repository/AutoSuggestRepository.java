// FILE: com.nifilili.search.repository.AutoSuggestRepository
package com.nifilili.search.repository;

import com.nifilili.core.exception.SearchExecutionException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for executing auto-suggest queries.
 * <p>
 * Provides separate query methods for business name suggestions (trigram similarity)
 * and category/vertical name suggestions (ILIKE pattern matching). These queries
 * are executed in parallel by {@link com.nifilili.search.service.AutoSuggestService}
 * using {@link java.util.concurrent.CompletableFuture}.
 * </p>
 */
@Repository
public class AutoSuggestRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Business name suggestion query using pg_trgm similarity.
     * Orders by similarity score descending, then by distance ascending if user location is provided.
     */
    private static final String BUSINESS_SUGGEST_SQL_WITH_LOCATION =
            "SELECT b.id, b.name, b.tole_name, b.municipality_id, " +
            "similarity(b.name, :query) AS sim_score, " +
            "(6371 * acos(" +
            "cos(radians(:userLat)) * cos(radians(b.latitude::float)) * " +
            "cos(radians(b.longitude::float) - radians(:userLng)) + " +
            "sin(radians(:userLat)) * sin(radians(b.latitude::float))" +
            ")) AS distance_km " +
            "FROM business_master b " +
            "WHERE b.status = 'PUBLISHED' " +
            "AND similarity(b.name, :query) > 0.1 " +
            "ORDER BY sim_score DESC, distance_km ASC " +
            "LIMIT :limit";

    /**
     * Business name suggestion query without user location.
     * Orders by similarity score descending only.
     */
    private static final String BUSINESS_SUGGEST_SQL_WITHOUT_LOCATION =
            "SELECT b.id, b.name, b.tole_name, b.municipality_id, " +
            "similarity(b.name, :query) AS sim_score, " +
            "NULL::float AS distance_km " +
            "FROM business_master b " +
            "WHERE b.status = 'PUBLISHED' " +
            "AND similarity(b.name, :query) > 0.1 " +
            "ORDER BY sim_score DESC " +
            "LIMIT :limit";

    /**
     * Category/vertical name suggestion query using ILIKE.
     * Searches the vertical name for partial matches.
     */
    private static final String CATEGORY_SUGGEST_SQL =
            "SELECT v.id, v.name " +
            "FROM business_vertical v " +
            "WHERE v.name ILIKE :pattern " +
            "ORDER BY v.name ASC " +
            "LIMIT :limit";

    /** Default limit for business suggestions within auto-suggest. */
    private static final int DEFAULT_BUSINESS_LIMIT = 5;

    /** Default limit for category suggestions within auto-suggest. */
    private static final int DEFAULT_CATEGORY_LIMIT = 3;

    /**
     * Fetches business name suggestions using trigram similarity.
     * <p>
     * Returns rows as Object arrays: [id, name, toleName, municipalityId, similarityScore, distanceKm].
     * </p>
     *
     * @param queryText the partial user input to match against business names
     * @param userLat   user's latitude (nullable)
     * @param userLng   user's longitude (nullable)
     * @param limit     maximum number of results (uses internal default if exceeds business limit)
     * @return list of raw result rows
     * @throws SearchExecutionException if the query fails
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> findBusinessSuggestions(
            String queryText,
            BigDecimal userLat,
            BigDecimal userLng,
            int limit
    ) {
        try {
            int effectiveLimit = Math.min(limit, DEFAULT_BUSINESS_LIMIT);
            boolean hasLocation = userLat != null && userLng != null;

            String sql = hasLocation
                    ? BUSINESS_SUGGEST_SQL_WITH_LOCATION
                    : BUSINESS_SUGGEST_SQL_WITHOUT_LOCATION;

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("query", queryText);
            query.setParameter("limit", effectiveLimit);

            if (hasLocation) {
                query.setParameter("userLat", userLat.doubleValue());
                query.setParameter("userLng", userLng.doubleValue());
            }

            return query.getResultList();
        } catch (Exception e) {
            throw new SearchExecutionException(
                    "Failed to execute business suggest query: " + e.getMessage(), e
            );
        }
    }

    /**
     * Fetches category/vertical name suggestions using ILIKE pattern matching.
     * <p>
     * Returns rows as Object arrays: [id, name].
     * </p>
     *
     * @param queryText the partial user input to match against category names
     * @param limit     maximum number of results (uses internal default if exceeds category limit)
     * @return list of raw result rows
     * @throws SearchExecutionException if the query fails
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> findCategorySuggestions(String queryText, int limit) {
        try {
            int effectiveLimit = Math.min(limit, DEFAULT_CATEGORY_LIMIT);
            Query query = entityManager.createNativeQuery(CATEGORY_SUGGEST_SQL);
            query.setParameter("pattern", "%" + queryText + "%");
            query.setParameter("limit", effectiveLimit);
            return query.getResultList();
        } catch (Exception e) {
            throw new SearchExecutionException(
                    "Failed to execute category suggest query: " + e.getMessage(), e
            );
        }
    }
}
