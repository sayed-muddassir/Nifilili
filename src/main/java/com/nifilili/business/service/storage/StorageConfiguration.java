package com.nifilili.business.service.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration properties for business file storage.
 * Centralized configuration for file upload, storage, and retrieval settings.
 * Configurable via application.yml under app.business-files prefix.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.business-files")
public class StorageConfiguration {

    private String basePath;
    private String storageProvider = "local";
    private String pathStrategy = "flat";
    private Long maxFileSize = 52428800L; // 50MB default
    private String allowedExtensions = ".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.jpg,.jpeg,.png,.gif,.zip";
    private String retentionPolicy = "keep-all";

    /**
     * Gets the set of allowed file extensions (lowercase, with leading dot).
     * Example: {".pdf", ".doc", ".xlsx"}
     */
    public Set<String> getAllowedExtensionsSet() {
        Set<String> extensions = new HashSet<>();
        if (allowedExtensions != null && !allowedExtensions.isBlank()) {
            String[] parts = allowedExtensions.split(",");
            for (String ext : parts) {
                String trimmed = ext.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    if (!trimmed.startsWith(".")) {
                        trimmed = "." + trimmed;
                    }
                    extensions.add(trimmed);
                }
            }
        }
        return extensions;
    }

    /**
     * Validates file extension against allowed extensions.
     */
    public boolean isExtensionAllowed(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return false;
        }

        String extension = fileName.substring(lastDot).toLowerCase();
        return getAllowedExtensionsSet().contains(extension);
    }

    /**
     * Validates file size against configured maximum.
     */
    public boolean isFileSizeAllowed(long fileSize) {
        return fileSize > 0 && fileSize <= maxFileSize;
    }
}
