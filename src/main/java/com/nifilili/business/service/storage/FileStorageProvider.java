package com.nifilili.business.service.storage;

import com.nifilili.business.service.BusinessFileService.DownloadedBusinessFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction for file storage operations. Implementations can be local filesystem,
 * AWS S3, Google Cloud Storage, etc. This enables seamless migration between
 * storage backends with minimal code changes.
 */
public interface FileStorageProvider {

    /**
     * Uploads a file and returns metadata about the stored file.
     *
     * @param file the MultipartFile to upload
     * @return UploadedFileInfo containing relative path, filename, and metadata
     * @throws IllegalArgumentException if file is null or invalid
     * @throws com.nifilili.core.exception.FileStorageException if upload fails
     */
    UploadedFileInfo uploadFile(MultipartFile file);

    /**
     * Downloads a file by its relative path.
     *
     * @param relativePath the relative path (after basePath) of the file
     * @return DownloadedBusinessFile containing the resource and metadata
     * @throws com.nifilili.core.exception.InvalidFilePathException if path is invalid or escapes base directory
     * @throws com.nifilili.core.exception.ResourceNotFoundException if file doesn't exist
     */
    DownloadedBusinessFile downloadFile(String relativePath);

    /**
     * Deletes a file by its relative path.
     *
     * @param relativePath the relative path of the file to delete
     * @throws com.nifilili.core.exception.InvalidFilePathException if path is invalid
     * @throws com.nifilili.core.exception.ResourceNotFoundException if file doesn't exist
     * @throws com.nifilili.core.exception.FileStorageException if deletion fails
     */
    void deleteFile(String relativePath);

    /**
     * Checks if a file exists at the given relative path.
     *
     * @param relativePath the relative path to check
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String relativePath);

    /**
     * Data class holding information about an uploaded file.
     */
    record UploadedFileInfo(
        String relativePath,        // relative path after basePath (e.g., "registration_v1.pdf")
        String fileName,            // stored filename (e.g., "registration_v1.pdf")
        String contentType,         // MIME type
        long size,                  // file size in bytes
        String contentHash,         // SHA-256 hash for duplicate detection
        String originalFileName,    // original filename as uploaded
        int version                 // version number (0 for first upload, 1 for _v1, etc.)
    ) {
    }
}
