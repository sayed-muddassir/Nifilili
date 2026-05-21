// FILE: com.nifilili.search.mapper.BusinessSearchResultMapper
package com.nifilili.search.mapper;

import com.nifilili.search.dto.response.SearchResultItem;
import com.nifilili.search.query.SearchFilterContext;
import com.nifilili.search.util.BusinessHoursEvaluator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Maps raw SQL query result rows to {@link SearchResultItem} DTOs.
 * <p>
 * This mapper is the ONLY component that knows the column index positions
 * of the native query results. All column-to-field mapping is centralized here,
 * making it the single point of change if the SELECT clause is modified.
 * </p>
 * <p>
 * Also responsible for:
 * <ul>
 *   <li>Formatting the business address from component fields</li>
 *   <li>Evaluating open/closed status via {@link BusinessHoursEvaluator}</li>
 *   <li>Applying post-fetch openNow filtering when requested</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessSearchResultMapper {

    private final BusinessHoursEvaluator businessHoursEvaluator;
    private final ObjectMapper objectMapper;

    // ──────────────────────────────────────────────
    // COLUMN INDEX CONSTANTS — matches BusinessSearchQueryBuilder.BASE_SELECT order
    // ──────────────────────────────────────────────

    /** Column index for business ID. */
    private static final int COL_ID = 0;

    /** Column index for business name. */
    private static final int COL_NAME = 1;

    /** Column index for vertical ID. */
    private static final int COL_VERTICAL_ID = 2;

    /** Column index for average rating. */
    private static final int COL_AVERAGE_RATING = 3;

    /** Column index for review count. */
    private static final int COL_REVIEW_COUNT = 4;

    /** Column index for KYC verification status. */
    private static final int COL_IS_KYC_VERIFIED = 5;

    /** Column index for business status. */
    private static final int COL_STATUS = 6;

    /** Column index for profile image URL. */
    private static final int COL_PROFILE_IMAGE_URL = 7;

    /** Column index for address field 1. */
    private static final int COL_ADDRESS_FIELD_1 = 8;

    /** Column index for address field 2. */
    private static final int COL_ADDRESS_FIELD_2 = 9;

    /** Column index for municipality ID. */
    private static final int COL_MUNICIPALITY_ID = 10;

    /** Column index for business hours JSONB. */
    private static final int COL_BUSINESS_HOURS = 11;

    /** Column index for tole name. */
    private static final int COL_TOLE_NAME = 12;

    /** Column index for ward number. */
    private static final int COL_WARD_NUMBER = 13;

    /** Column index for search rank (appended dynamically). */
    private static final int COL_SEARCH_RANK = 14;

    /** Column index for distance km (appended dynamically). */
    private static final int COL_DISTANCE_KM = 15;

    /** Scale for rounding distance values. */
    private static final int DISTANCE_SCALE = 2;

    /**
     * Maps a list of raw query result rows to SearchResultItem DTOs.
     * <p>
     * When the openNow filter is active, businesses whose open status
     * cannot be determined or who are currently closed are excluded.
     * </p>
     *
     * @param rows          the raw result rows from the repository
     * @param filterContext the filter context for applying post-fetch filters
     * @return a list of mapped and filtered SearchResultItem DTOs
     */
    public List<SearchResultItem> mapResults(
            List<Object[]> rows,
            SearchFilterContext filterContext
    ) {
        List<SearchResultItem> results = new ArrayList<>();
        boolean applyOpenNowFilter = Boolean.TRUE.equals(filterContext.getOpenNow());

        for (Object[] row : rows) {
            SearchResultItem item = mapSingleRow(row);

            if (applyOpenNowFilter) {
                if (!Boolean.TRUE.equals(item.getIsOpenNow())) {
                    continue;
                }
            }

            results.add(item);
        }

        return results;
    }

    /**
     * Maps a single raw result row to a SearchResultItem DTO.
     *
     * @param row the raw Object array from the native query
     * @return a fully populated SearchResultItem
     */
    private SearchResultItem mapSingleRow(Object[] row) {
        Map<String, Object> businessHours = parseBusinessHours(row[COL_BUSINESS_HOURS]);
        Boolean isOpenNow = businessHoursEvaluator.isOpenNow(businessHours);

        return SearchResultItem.builder()
                .id(toLong(row[COL_ID]))
                .name(toStr(row[COL_NAME]))
                .verticalId(toLong(row[COL_VERTICAL_ID]))
                .averageRating(toBigDecimal(row[COL_AVERAGE_RATING]))
                .reviewCount(toInteger(row[COL_REVIEW_COUNT]))
                .isKycVerified(toBoolean(row[COL_IS_KYC_VERIFIED]))
                .status(toStr(row[COL_STATUS]))
                .profileImageUrl(toStr(row[COL_PROFILE_IMAGE_URL]))
                .formattedAddress(formatAddress(row))
                .municipalityId(toLong(row[COL_MUNICIPALITY_ID]))
                .distanceKm(toRoundedDistance(row[COL_DISTANCE_KM]))
                .isOpenNow(isOpenNow)
                .build();
    }

    /**
     * Formats a human-readable address from the component address fields.
     *
     * @param row the raw result row
     * @return a formatted address string
     */
    private String formatAddress(Object[] row) {
        StringBuilder address = new StringBuilder();

        String toleName = toStr(row[COL_TOLE_NAME]);
        Integer wardNumber = toInteger(row[COL_WARD_NUMBER]);
        String addressField1 = toStr(row[COL_ADDRESS_FIELD_1]);
        String addressField2 = toStr(row[COL_ADDRESS_FIELD_2]);

        if (toleName != null && !toleName.isBlank()) {
            address.append(toleName);
        }
        if (wardNumber != null) {
            if (!address.isEmpty()) address.append(", ");
            address.append("Ward ").append(wardNumber);
        }
        if (addressField1 != null && !addressField1.isBlank()) {
            if (!address.isEmpty()) address.append(", ");
            address.append(addressField1);
        }
        if (addressField2 != null && !addressField2.isBlank()) {
            if (!address.isEmpty()) address.append(", ");
            address.append(addressField2);
        }

        return address.isEmpty() ? null : address.toString();
    }

    /**
     * Parses the business hours JSONB column into a Map.
     *
     * @param value the raw column value (String or already parsed Map)
     * @return the parsed map, or null if parsing fails
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseBusinessHours(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        if (value instanceof String jsonStr) {
            try {
                return objectMapper.readValue(jsonStr, new TypeReference<>() {});
            } catch (Exception e) {
                log.warn("Failed to parse business hours JSON: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    // ──────────────────────────────────────────────
    // TYPE CONVERSION HELPERS
    // ──────────────────────────────────────────────

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number num) return num.longValue();
        return null;
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number num) return num.intValue();
        return null;
    }

    private String toStr(Object value) {
        if (value == null) return null;
        return value.toString();
    }

    private Boolean toBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean bool) return bool;
        return null;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number num) return BigDecimal.valueOf(num.doubleValue());
        return null;
    }

    private BigDecimal toRoundedDistance(Object value) {
        BigDecimal distance = toBigDecimal(value);
        if (distance == null) return null;
        return distance.setScale(DISTANCE_SCALE, RoundingMode.HALF_UP);
    }
}
