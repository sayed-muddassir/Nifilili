// FILE: com.nifilili.search.query.SearchQueryResult
package com.nifilili.search.query;

import lombok.Getter;

import java.util.Map;

/**
 * Holds the final constructed SQL query string and its bound parameters.
 * <p>
 * This is the output of {@link BusinessSearchQueryBuilder#build()} and the
 * input to the repository layer. It decouples query construction from
 * query execution, ensuring the repository remains a thin execution layer.
 * </p>
 */
@Getter
public class SearchQueryResult {

    /** The complete native SQL query string with named parameter placeholders. */
    private final String sql;

    /** The complete native SQL count query string for pagination total. */
    private final String countSql;

    /** Named parameters to bind to the query. Keys match :paramName placeholders. */
    private final Map<String, Object> parameters;

    /**
     * Constructs a new SearchQueryResult.
     *
     * @param sql        the data-fetch SQL query string
     * @param countSql   the count SQL query string for total results
     * @param parameters the named parameters to bind
     */
    public SearchQueryResult(String sql, String countSql, Map<String, Object> parameters) {
        this.sql = sql;
        this.countSql = countSql;
        this.parameters = parameters;
    }
}
