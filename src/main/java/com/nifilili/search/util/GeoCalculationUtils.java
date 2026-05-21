// FILE: com.nifilili.search.util.GeoCalculationUtils
package com.nifilili.search.util;

/**
 * Utility class for geographic calculations used in distance-based search.
 * <p>
 * Provides methods for computing bounding boxes and Haversine distances
 * without requiring PostGIS. The bounding box approach enables efficient
 * use of B-tree indexes on latitude/longitude columns.
 * </p>
 * <p>
 * This class is non-instantiable and stateless — all methods are static.
 * </p>
 */
public final class GeoCalculationUtils {

    /** Mean radius of the Earth in kilometers. */
    private static final double EARTH_RADIUS_KM = 6371.0;

    /** Approximate km per degree of latitude. */
    private static final double KM_PER_DEGREE_LAT = 111.0;

    /**
     * Private constructor to prevent instantiation.
     */
    private GeoCalculationUtils() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    /**
     * Computes a bounding box around a center point for a given radius.
     * <p>
     * The bounding box is used as a fast pre-filter in SQL queries before
     * applying the exact Haversine formula. This enables efficient use of
     * B-tree indexes on latitude and longitude columns.
     * </p>
     * <p>
     * Returns an array of [latMin, latMax, lngMin, lngMax].
     * </p>
     *
     * @param centerLat  the center latitude in degrees
     * @param centerLng  the center longitude in degrees
     * @param radiusKm   the search radius in kilometers
     * @return a double array [latMin, latMax, lngMin, lngMax]
     * @throws IllegalArgumentException if radiusKm is non-positive
     */
    public static double[] computeBoundingBox(double centerLat, double centerLng, double radiusKm) {
        if (radiusKm <= 0) {
            throw new IllegalArgumentException("Radius must be positive, got: " + radiusKm);
        }

        double latDelta = radiusKm / KM_PER_DEGREE_LAT;
        double lngDelta = radiusKm / (KM_PER_DEGREE_LAT * Math.cos(Math.toRadians(centerLat)));

        double latMin = centerLat - latDelta;
        double latMax = centerLat + latDelta;
        double lngMin = centerLng - lngDelta;
        double lngMax = centerLng + lngDelta;

        return new double[]{latMin, latMax, lngMin, lngMax};
    }

    /**
     * Computes the Haversine distance between two geographic coordinates.
     * <p>
     * This Java-side calculation is used for post-fetch verification
     * when exact distance filtering is needed beyond the SQL bounding box.
     * </p>
     *
     * @param lat1 latitude of point 1 in degrees
     * @param lng1 longitude of point 1 in degrees
     * @param lat2 latitude of point 2 in degrees
     * @param lng2 longitude of point 2 in degrees
     * @return the distance in kilometers between the two points
     */
    public static double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
