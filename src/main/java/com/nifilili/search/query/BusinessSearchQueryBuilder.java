// FILE: com.nifilili.search.query.BusinessSearchQueryBuilder
package com.nifilili.search.query;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nifilili.search.strategy.SortStrategy;
import com.nifilili.search.util.GeoCalculationUtils;

/**
 * Fluent builder for constructing native PostgreSQL search queries dynamically.
 * <p>
 * Implements the Builder design pattern. Each {@code with*()} method independently
 * appends a self-contained SQL fragment and its parameters. Methods can be called
 * in any order and are mutually independent — adding a new filter means adding
 * one new {@code with*()} method without modifying any existing methods.
 * </p>
 * <p>
 * SQL constants are defined as named static finals to avoid magic strings.
 * The builder never executes queries — it only constructs them.
 * </p>
 *
 * <b>Usage:</b>
 * <pre>{@code
 * SearchQueryResult result = new BusinessSearchQueryBuilder()
 *     .withKeyword(context)
 *     .withGeoFilter(context)
 *     .withVerificationFilter(context)
 *     .withRatingFilter(context)
 *     .withSort(strategy)
 *     .withPagination(0, 20)
 *     .build();
 * }</pre>
 */
public class BusinessSearchQueryBuilder {

    // ──────────────────────────────────────────────
    // SQL FRAGMENT CONSTANTS
    // ──────────────────────────────────────────────

    /**
     * Handle very short keywords separately.
     * Example:
     * a,
     * ab,
     * pi
     */
    private static final String SHORT_SEARCH_CONDITION =
            " AND lower(b.name) ILIKE lower(:keywordPrefix)";

    private static final int MIN_FTS_LENGTH = 2;

    /**
     * Base SELECT clause with all required columns for result mapping.
     */
    private static final String BASE_SELECT =
            "SELECT b.id, b.name, b.vertical_id, b.average_rating, b.review_count, "
                    + "b.is_kyc_verified, b.status, b.profile_image_url, "
                    + "b.address_field_1, b.address_field_2, b.municipality_id, "
                    + "b.business_hours, b.tole_name, b.ward_number";

    /**
     * Combined search score:
     *
     * 1. Full text rank
     * 2. Word similarity for typo tolerance
     *
     * Examples:
     * pizza  -> Pizza Palace
     * piza   -> Pizza Palace
     * plumber -> ABC Plumbers
     */
    private static final String SEARCH_RANK_SELECT =
            ", ("
                    + " ts_rank(b.search_vector, plainto_tsquery('english', :keyword))"
                    + " + (word_similarity(lower(b.name), lower(:keyword)) * 0.30)"
                    + " ) AS search_rank";

    /**
     * Search condition:
     *
     * - Full text search
     * - Typo tolerant matching
     */
    private static final String SEARCH_CONDITION =
            " AND ("
                    + " b.search_vector @@ plainto_tsquery('english', :keyword)"
                    + " OR word_similarity(lower(b.name), lower(:keyword)) > 0.35"
                    + " )";

    /**
     * Default search rank when no keyword is provided.
     */
    private static final String DEFAULT_RANK_SELECT = ", (1000 - ASCII(UPPER(LEFT(b.name, 1)))) AS search_rank";

    /**
     * Haversine distance computation column.
     */
    private static final String DISTANCE_SELECT =
            """
                    , (
                        6371 * acos(
                            cos(radians(:userLat))
                            * cos(radians(b.latitude::float))
                            * cos(radians(b.longitude::float) - radians(:userLng))
                            + sin(radians(:userLat))
                            * sin(radians(b.latitude::float))
                        )
                    ) AS distance_km
                    """;

    /**
     * Default distance column when user location is not provided.
     */
    private static final String DEFAULT_DISTANCE_SELECT = ", NULL::float AS distance_km";

    private static final String DISTANCE_FILTER =
            """
                    
                    AND (
                        6371 * acos(
                            cos(radians(:userLat))
                            * cos(radians(b.latitude::float))
                            * cos(radians(b.longitude::float) - radians(:userLng))
                            + sin(radians(:userLat))
                            * sin(radians(b.latitude::float))
                        )
                    ) <= :distanceKm
                    """;
    /**
     * Base FROM clause.
     */
    private static final String BASE_FROM = " FROM business_master b";

    /**
     * Base WHERE clause ensuring only published businesses are searchable.
     */
    private static final String BASE_WHERE = " WHERE b.status = 'PUBLISHED'";

    /**
     * Bounding box latitude filter for geo queries (uses B-tree index).
     */
    private static final String GEO_BOUNDING_BOX_LAT =
            " AND b.latitude BETWEEN :latMin AND :latMax";

    /**
     * Bounding box longitude filter for geo queries (uses B-tree index).
     */
    private static final String GEO_BOUNDING_BOX_LNG =
            " AND b.longitude BETWEEN :lngMin AND :lngMax";

    /**
     * Municipality ID filter condition.
     */
    private static final String MUNICIPALITY_CONDITION =
            " AND b.municipality_id = :municipalityId";

    /**
     * KYC verification filter condition.
     */
    private static final String VERIFIED_CONDITION =
            " AND b.is_kyc_verified = true";

    /**
     * Minimum rating filter condition.
     */
    private static final String RATING_CONDITION =
            " AND b.average_rating >= :minRating";

    /**
     * JSONB vertical attribute filter condition template.
     */
    private static final String VERTICAL_ATTR_CONDITION_TEMPLATE =
            " AND b.business_hours IS NOT NULL";

    // ──────────────────────────────────────────────
    // BUILDER STATE
    // ──────────────────────────────────────────────

    private final List<String> selectClauses = new ArrayList<>();
    private final List<String> whereClauses = new ArrayList<>();
    private final Map<String, Object> parameters = new HashMap<>();
    private String orderByClause = "b.id DESC";
    private int offset;
    private int limit = 20;
    private boolean hasKeyword;
    private boolean hasGeoFilter;

    public BusinessSearchQueryBuilder withKeyword(SearchFilterContext context) {
        if (!context.hasKeyword()) {
            selectClauses.add(DEFAULT_RANK_SELECT);
            return this;
        }

        String keyword = context.getKeyword().trim();

        if (keyword.isBlank()) {
            selectClauses.add(DEFAULT_RANK_SELECT);
            return this;
        }
        this.hasKeyword = true;

        /*
         * Single-character searches:
         *
         * a
         * p
         * e
         *
         * FTS ignores many of these because they are stop words.
         * Trigram similarity is also ineffective.
         *
         * Use wildcard matching instead.
         */
        if (keyword.length() <= MIN_FTS_LENGTH) {
            selectClauses.add(DEFAULT_RANK_SELECT);
            whereClauses.add(SHORT_SEARCH_CONDITION);
            parameters.put("keywordPrefix", "%" + keyword + "%");
            return this;
        }

        /*
         * Full text search + typo tolerance
         */
        selectClauses.add(SEARCH_RANK_SELECT);
        whereClauses.add(SEARCH_CONDITION);
        parameters.put("keyword", keyword);
        return this;
    }

    /**
     * Adds geographic bounding-box and Haversine distance calculation.
     * <p>
     * Step 1: Applies a bounding box filter using latitude/longitude B-tree
     * indexes to quickly eliminate distant rows.
     * Step 2: Computes exact Haversine distance as a selected column.
     * </p>
     *
     * @param context the filter context containing geo parameters
     * @return this builder for fluent chaining
     */
    public BusinessSearchQueryBuilder withGeoFilter(SearchFilterContext context) {
        if (context.hasGeoFilter()) {
            this.hasGeoFilter = true;
            BigDecimal userLat = context.getUserLat();
            BigDecimal userLng = context.getUserLng();
            BigDecimal distanceKm = context.getDistanceKm();

            // Compute bounding box in Java
            double[] boundingBox = GeoCalculationUtils.computeBoundingBox(
                    userLat.doubleValue(), userLng.doubleValue(), distanceKm.doubleValue());

            // Add Haversine distance to SELECT
            selectClauses.add(DISTANCE_SELECT);

            // Add bounding box WHERE conditions
            whereClauses.add(GEO_BOUNDING_BOX_LAT);
            whereClauses.add(GEO_BOUNDING_BOX_LNG);
            whereClauses.add(DISTANCE_FILTER);

            parameters.put("userLat", userLat.doubleValue());
            parameters.put("userLng", userLng.doubleValue());
            parameters.put("latMin", boundingBox[0]);
            parameters.put("latMax", boundingBox[1]);
            parameters.put("lngMin", boundingBox[2]);
            parameters.put("lngMax", boundingBox[3]);
            parameters.put("distanceKm", distanceKm.doubleValue());
        } else {
            selectClauses.add(DEFAULT_DISTANCE_SELECT);
        }
        return this;
    }

    /**
     * Adds municipality location filter.
     *
     * @param context the filter context containing municipality ID
     * @return this builder for fluent chaining
     */
    public BusinessSearchQueryBuilder withLocationFilter(SearchFilterContext context) {
        if (context.getMunicipalityId() != null) {
            whereClauses.add(MUNICIPALITY_CONDITION);
            parameters.put("municipalityId", context.getMunicipalityId());
        }
        return this;
    }

    /**
     * Adds KYC verification filter.
     * <p>
     * Only applied when {@code verifiedOnly} is explicitly set to true.
     * All published businesses are searchable regardless of verification status
     * unless this filter is activated.
     * </p>
     *
     * @param context the filter context containing verification preference
     * @return this builder for fluent chaining
     */
    public BusinessSearchQueryBuilder withVerificationFilter(SearchFilterContext context) {
        if (Boolean.TRUE.equals(context.getVerifiedOnly())) {
            whereClauses.add(VERIFIED_CONDITION);
        }
        return this;
    }

    /**
     * Adds minimum rating filter.
     *
     * @param context the filter context containing minimum rating
     * @return this builder for fluent chaining
     */
    public BusinessSearchQueryBuilder withRatingFilter(SearchFilterContext context) {
        if (context.getMinRating() != null) {
            whereClauses.add(RATING_CONDITION);
            parameters.put("minRating", context.getMinRating());
        }
        return this;
    }

    /**
     * Adds vertical-specific attribute filters using JSONB containment.
     * <p>
     * Each attribute key-value pair is translated into a JSONB containment check.
     * Multiple values for the same key use OR logic; different keys use AND logic.
     * </p>
     *
     * @param context the filter context containing vertical attributes
     * @return this builder for fluent chaining
     */
    public BusinessSearchQueryBuilder withVerticalAttributeFilter(SearchFilterContext context) {
        Map<String, List<String>> attrs = context.getVerticalAttributes();
        if (attrs == null || attrs.isEmpty()) {
            return this;
        }

        int attrIndex = 0;
        for (Map.Entry<String, List<String>> entry : attrs.entrySet()) {
            String attrKey = entry.getKey();
            List<String> attrValues = entry.getValue();
            if (attrValues == null || attrValues.isEmpty()) {
                continue;
            }

            // Build OR conditions for multiple values of the same attribute
            List<String> valueConditions = new ArrayList<>();
            for (int i = 0; i < attrValues.size(); i++) {
                String paramName = "vertAttr_" + attrIndex + "_" + i;
                valueConditions.add(
                        "b.business_hours->'" + attrKey + "' @> ('\"' || :" + paramName + " || '\"')::jsonb");
                parameters.put(paramName, attrValues.get(i));
            }

            String combinedCondition = " AND (" + String.join(" OR ", valueConditions) + ")";
            whereClauses.add(combinedCondition);
            attrIndex++;
        }
        return this;
    }

    /**
     * Sets the ORDER BY clause using the provided sort strategy.
     * <p>
     * Delegates to the strategy's {@code toOrderByClause()} method,
     * ensuring the builder never contains sort-specific if/else logic.
     * </p>
     *
     * @param strategy the resolved sort strategy
     * @return this builder for fluent chaining
     */
    public BusinessSearchQueryBuilder withSort(SortStrategy strategy) {
        if (strategy != null) {
            this.orderByClause = strategy.toOrderByClause();
        }
        return this;
    }

    /**
     * Sets pagination parameters.
     *
     * @param page the zero-based page number
     * @param size the number of results per page
     * @return this builder for fluent chaining
     */
    public BusinessSearchQueryBuilder withPagination(int page, int size) {
        this.offset = page * size;
        this.limit = size;
        return this;
    }

    /**
     * Builds the final SQL query and count query with all accumulated fragments.
     *
     * @return a {@link SearchQueryResult} containing the data query, count query, and parameters
     */
    public SearchQueryResult build() {
        StringBuilder dataSql = new StringBuilder();
        StringBuilder countSql = new StringBuilder();

        // Build SELECT
        dataSql.append(BASE_SELECT);
        for (String selectClause : selectClauses) {
            dataSql.append(selectClause);
        }

        // Build FROM
        dataSql.append(BASE_FROM);

        // Build WHERE
        dataSql.append(BASE_WHERE);
        for (String whereClause : whereClauses) {
            dataSql.append(whereClause);
        }

        // Build COUNT query (same WHERE but simplified SELECT)
        countSql.append("SELECT COUNT(*)");
        countSql.append(BASE_FROM);
        countSql.append(BASE_WHERE);
        for (String whereClause : whereClauses) {
            countSql.append(whereClause);
        }

        // Build ORDER BY
        dataSql.append(" ORDER BY ").append(orderByClause);

        // Build LIMIT/OFFSET
        dataSql.append(" LIMIT :limit OFFSET :offset");
        parameters.put("limit", limit);
        parameters.put("offset", offset);

        return new SearchQueryResult(dataSql.toString(), countSql.toString(), parameters);
    }
}
