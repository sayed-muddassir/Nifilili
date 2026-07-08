package com.nifilili.business.service.storage;

import org.springframework.stereotype.Component;

/**
 * Flat path strategy: stores all files in the root storage directory.
 * Example: "registration.pdf", "registration_v1.pdf", "registration_v2.pdf"
 */
@Component
public class FlatPathStrategy implements PathStrategy {

    @Override
    public String generatePath(String fileName, int version) {
        if (version == 0) {
            return fileName;
        }
        return insertVersionSuffix(fileName, version);
    }

    /**
     * Inserts version suffix before file extension.
     * Example: "registration.pdf" with version 1 → "registration_v1.pdf"
     */
    private String insertVersionSuffix(String fileName, int version) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName + "_v" + version;
        }
        String nameWithoutExtension = fileName.substring(0, lastDotIndex);
        String extension = fileName.substring(lastDotIndex);
        return nameWithoutExtension + "_v" + version + extension;
    }
}
