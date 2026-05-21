// FILE: com.nifilili.search.util.BusinessHoursEvaluator
package com.nifilili.search.util;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Evaluates whether a business is currently open based on its businessHours JSONB data.
 * <p>
 * The openNow filter is applied in the service layer (post-fetch), not in SQL,
 * because the businessHours structure is a denormalized JSONB field that cannot
 * be efficiently queried with SQL time comparisons.
 * </p>
 * <p>
 * Expected businessHours JSON structure:
 * <pre>{@code
 * {
 *   "monday":    { "open": "09:00", "close": "21:00", "isClosed": false },
 *   "tuesday":   { "open": "09:00", "close": "21:00", "isClosed": false },
 *   "wednesday": { "open": "09:00", "close": "17:00", "isClosed": false },
 *   "thursday":  { "open": "09:00", "close": "21:00", "isClosed": false },
 *   "friday":    { "open": "09:00", "close": "22:00", "isClosed": false },
 *   "saturday":  { "open": "10:00", "close": "22:00", "isClosed": false },
 *   "sunday":    { "open": "00:00", "close": "00:00", "isClosed": true }
 * }
 * }</pre>
 * </p>
 */
@Component
public class BusinessHoursEvaluator {

    /** JSON key for opening time. */
    private static final String KEY_OPEN = "open";

    /** JSON key for closing time. */
    private static final String KEY_CLOSE = "close";

    /** JSON key indicating the business is closed on this day. */
    private static final String KEY_IS_CLOSED = "isClosed";

    /**
     * Determines whether a business is currently open based on its hours.
     * <p>
     * Returns {@code null} if businessHours is null or the current day's
     * data is missing or unparseable, indicating that open/closed status
     * cannot be determined.
     * </p>
     *
     * @param businessHours the businessHours JSONB map from the business entity
     * @return {@code true} if currently open, {@code false} if closed,
     *         or {@code null} if hours data is unavailable
     */
    public Boolean isOpenNow(Map<String, Object> businessHours) {
        if (businessHours == null || businessHours.isEmpty()) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        String dayKey = mapDayOfWeek(now.getDayOfWeek());

        Object dayDataObj = businessHours.get(dayKey);
        if (!(dayDataObj instanceof Map)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> dayData = (Map<String, Object>) dayDataObj;

        return evaluateDayHours(dayData, now.toLocalTime());
    }

    /**
     * Evaluates whether the current time falls within the day's open/close range.
     *
     * @param dayData     the day-specific hours data
     * @param currentTime the current local time
     * @return true if open, false if closed, null if data is unparseable
     */
    private Boolean evaluateDayHours(Map<String, Object> dayData, LocalTime currentTime) {
        // Check if explicitly marked as closed
        Object isClosedObj = dayData.get(KEY_IS_CLOSED);
        if (isClosedObj instanceof Boolean && (Boolean) isClosedObj) {
            return false;
        }

        Object openObj = dayData.get(KEY_OPEN);
        Object closeObj = dayData.get(KEY_CLOSE);

        if (!(openObj instanceof String) || !(closeObj instanceof String)) {
            return null;
        }

        try {
            LocalTime openTime = LocalTime.parse((String) openObj);
            LocalTime closeTime = LocalTime.parse((String) closeObj);

            // Handle overnight hours (e.g., open 20:00, close 02:00)
            if (closeTime.isAfter(openTime)) {
                return !currentTime.isBefore(openTime) && currentTime.isBefore(closeTime);
            } else {
                // Overnight: open if current is after open OR before close
                return !currentTime.isBefore(openTime) || currentTime.isBefore(closeTime);
            }
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Maps Java's {@link DayOfWeek} to the lowercase day key used in the JSONB structure.
     *
     * @param dayOfWeek the day of week
     * @return lowercase day name (e.g., "monday", "tuesday")
     */
    private String mapDayOfWeek(DayOfWeek dayOfWeek) {
        return dayOfWeek.name().toLowerCase();
    }
}
