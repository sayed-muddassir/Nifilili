package com.nifilili.business.service.storage;

import com.nifilili.business.service.BusinessFileService.DownloadedBusinessFile;
import com.nifilili.core.exception.FileConflictException;
import com.nifilili.core.exception.FileStorageException;
import com.nifilili.core.exception.InvalidFilePathException;
import com.nifilili.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Local filesystem implementation of FileStorageProvider.
 * Stores files on the server's filesystem with versioning support.
 * Features: atomic operations, SHA-256 hashing, duplicate detection, cleanup on failure.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalFileStorageProvider implements FileStorageProvider {

    private final PathStrategy pathStrategy;
    private final StorageConfiguration storageConfiguration;

    @Value("${app.business-files.base-path}")
    private String basePath;

    private Path getBasePath() {
        return Paths.get(basePath).toAbsolutePath().normalize();
    }

    @Override
    public UploadedFileInfo uploadFile(MultipartFile file) {
        if (file == null) {
            log.warn("Upload attempt with null file");
            throw new IllegalArgumentException("File is required");
        }

        String originalFileName = file.getOriginalFilename();
        String safeFileName = sanitizeFileName(originalFileName);
        log.debug("Uploading file: original={}, safe={}", originalFileName, safeFileName);

        // Validate file extension
        if (!storageConfiguration.isExtensionAllowed(safeFileName)) {
            log.warn("File extension not allowed: {}", safeFileName);
            throw new IllegalArgumentException("File extension not allowed. Allowed: " + storageConfiguration.getAllowedExtensions());
        }

        // Validate file size
        long fileSize = file.getSize();
        if (!storageConfiguration.isFileSizeAllowed(fileSize)) {
            log.warn("File size exceeds limit: {} > {}", fileSize, storageConfiguration.getMaxFileSize());
            throw new IllegalArgumentException("File size exceeds maximum allowed: " + storageConfiguration.getMaxFileSize() + " bytes");
        }

        Path tempFile;
        String contentHash;
        try {
            Path basePathObj = getBasePath();
            Files.createDirectories(basePathObj);
            tempFile = Files.createTempFile(basePathObj, "upload-", ".tmp");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream inputStream = new DigestInputStream(file.getInputStream(), digest)) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            contentHash = toHex(digest.digest());
            log.debug("File uploaded to temp location: {}, hash: {}", tempFile, contentHash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new FileStorageException("Failed to initialize file hashing", e);
        } catch (IOException e) {
            log.error("Failed to store file to temp location", e);
            throw new FileStorageException("Failed to store file", e);
        }

        // Find next version number
        int version = findNextVersion(safeFileName);
        String versionedFileName = pathStrategy.generatePath(safeFileName, version);
        Path targetPath = getBasePath().resolve(versionedFileName).normalize();

        // Validate target path doesn't escape basePath
        if (!targetPath.startsWith(getBasePath())) {
            cleanupTempFile(tempFile);
            log.error("Invalid target path (escapes basePath): {}", targetPath);
            throw new InvalidFilePathException("Invalid upload file path");
        }

        // Final check: file shouldn't already exist
        if (Files.exists(targetPath)) {
            cleanupTempFile(tempFile);
            log.warn("File already exists at target path: {}", targetPath);
            throw new FileConflictException("That file is already present. Please upload a new one or a different one.");
        }

        try {
            Files.move(tempFile, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            log.info("File successfully moved to: {}", targetPath);
        } catch (IOException e) {
            cleanupTempFile(tempFile);
            log.error("Failed to finalize file storage", e);
            throw new FileStorageException("Failed to finalize file storage", e);
        } finally {
            cleanupTempFile(tempFile);
        }

        String contentType = StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        String relativePath = versionedFileName;

        log.info("File uploaded successfully: relativePath={}, version={}, size={}", relativePath, version, fileSize);
        return new UploadedFileInfo(relativePath, versionedFileName, contentType, fileSize, contentHash, originalFileName, version);
    }

    @Override
    public DownloadedBusinessFile downloadFile(String relativePath) {
        log.debug("Downloading file: {}", relativePath);
        Path normalizedFilePath = validateAndNormalizeStoredPath(relativePath);

        if (!normalizedFilePath.startsWith(getBasePath())) {
            log.warn("Invalid file path (escapes basePath): {}", normalizedFilePath);
            throw new InvalidFilePathException("Invalid file path");
        }

        if (!Files.exists(normalizedFilePath) || !Files.isRegularFile(normalizedFilePath)) {
            log.warn("File not found: {}", normalizedFilePath);
            throw new ResourceNotFoundException("File not found");
        }

        String contentType = detectContentType(normalizedFilePath);
        String fileName = normalizedFilePath.getFileName().toString();
        long size = fileSize(normalizedFilePath);

        log.info("File ready for download: {}", fileName);
        return new DownloadedBusinessFile(
                new FileSystemResource(normalizedFilePath),
                fileName,
                contentType,
                size
        );
    }

    @Override
    public void deleteFile(String relativePath) {
        log.debug("Deleting file: {}", relativePath);
        Path normalizedFilePath = validateAndNormalizeStoredPath(relativePath);

        if (!normalizedFilePath.startsWith(getBasePath())) {
            log.warn("Invalid file path for deletion (escapes basePath): {}", normalizedFilePath);
            throw new InvalidFilePathException("Invalid file path");
        }

        if (!Files.exists(normalizedFilePath)) {
            log.warn("Cannot delete: file not found: {}", normalizedFilePath);
            throw new ResourceNotFoundException("File not found");
        }

        try {
            Files.delete(normalizedFilePath);
            log.info("File deleted: {}", normalizedFilePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", normalizedFilePath, e);
            throw new FileStorageException("Failed to delete file", e);
        }
    }

    @Override
    public boolean fileExists(String relativePath) {
        try {
            Path normalizedFilePath = validateAndNormalizeStoredPath(relativePath);
            boolean exists = Files.exists(normalizedFilePath) && Files.isRegularFile(normalizedFilePath);
            log.debug("File existence check: {} = {}", relativePath, exists);
            return exists;
        } catch (Exception e) {
            log.debug("File existence check failed for: {}", relativePath, e);
            return false;
        }
    }

    /**
     * Finds the next available version number for a filename.
     * Returns 0 if file doesn't exist, otherwise returns next version number.
     * Uses synchronized block for thread-safe version detection.
     */
    private int findNextVersion(String fileName) {
        synchronized (fileName.intern()) {
            Path basePath = getBasePath();
            int version = 0;

            while (true) {
                String pathToCheck = pathStrategy.generatePath(fileName, version);
                Path filePath = basePath.resolve(pathToCheck);

                if (!Files.exists(filePath)) {
                    log.debug("Next available version for {}: {}", fileName, version);
                    return version;
                }
                version++;

                // Safety check: prevent infinite loop (max 1000 versions)
                if (version > 1000) {
                    log.error("Too many versions of file: {}", fileName);
                    throw new IllegalArgumentException("Too many file versions. Manual cleanup required.");
                }
            }
        }
    }

    /**
     * Sanitizes filename to prevent directory traversal and invalid characters.
     */
    private String sanitizeFileName(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            return "file";
        }

        String cleanPath = StringUtils.cleanPath(originalFileName).trim();
        if (!StringUtils.hasText(cleanPath) || cleanPath.contains("..") || ".".equals(cleanPath)) {
            return "file";
        }

        Path fileNamePath = Paths.get(cleanPath);
        if (fileNamePath.getNameCount() == 0) {
            return "file";
        }

        String fileName = fileNamePath.getFileName().toString();
        if (!StringUtils.hasText(fileName) || fileName.contains("/") || fileName.contains("\\")
                || ".".equals(fileName) || "..".equals(fileName)) {
            return "file";
        }
        return fileName;
    }

    /**
     * Validates and normalizes a file path to ensure it's within basePath.
     */
    private Path validateAndNormalizeStoredPath(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new InvalidFilePathException("File path is required");
        }

        Path path = Paths.get(filePath).normalize();
        Path absolutePath = getBasePath().resolve(path).normalize();

        if (!absolutePath.startsWith(getBasePath())) {
            throw new InvalidFilePathException("Invalid file path");
        }

        return absolutePath;
    }

    /**
     * Detects MIME type of a file.
     */
    private String detectContentType(Path filePath) {
        try {
            String contentType = Files.probeContentType(filePath);
            return StringUtils.hasText(contentType)
                    ? contentType
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        } catch (IOException e) {
            log.debug("Failed to detect content type for: {}", filePath);
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    /**
     * Gets the size of a file.
     */
    private long fileSize(Path filePath) {
        try {
            return Files.size(filePath);
        } catch (IOException e) {
            log.error("Failed to read file size: {}", filePath, e);
            throw new FileStorageException("Failed to read file size", e);
        }
    }

    /**
     * Converts bytes to hex string.
     */
    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }

    /**
     * Best-effort cleanup of temporary files.
     */
    private void cleanupTempFile(Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
            log.debug("Temp file cleaned up: {}", tempFile);
        } catch (IOException ignored) {
            log.debug("Failed to cleanup temp file: {}", tempFile);
        }
    }
}
