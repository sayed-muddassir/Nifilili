package com.nifilili.business.service.storage;

/**
 * Strategy interface for generating file paths within the storage system.
 * Allows flexible folder organization without requiring code changes.
 * Examples: flat structure, dated folders, module-based organization.
 */
public interface PathStrategy {

    /**
     * Generates a path for storing a file.
     *
     * @param fileName the sanitized filename
     * @param version the version number (0 for first upload, 1 for _v1, etc.)
     * @return relative path within the storage root (e.g., "registration_v1.pdf")
     */
    String generatePath(String fileName, int version);
}
