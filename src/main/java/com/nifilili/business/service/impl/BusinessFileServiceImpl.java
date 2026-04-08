package com.nifilili.business.service.impl;

import com.nifilili.business.dto.response.BusinessFileUploadResponse;
import com.nifilili.business.service.BusinessFileService;
import com.nifilili.core.exception.FileConflictException;
import com.nifilili.core.exception.FileStorageException;
import com.nifilili.core.exception.InvalidFilePathException;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
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

@Service
public class BusinessFileServiceImpl implements BusinessFileService {

    private final Path basePath;

    public BusinessFileServiceImpl(
            @Value("${app.business-files.base-path}") String basePath
    ) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
    }

    @Override
    public BusinessFileUploadResponse uploadFile(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("File is required");
        }

        String safeFileName = sanitizeFileName(file.getOriginalFilename());

        Path tempFile;
        String contentHash;
        try {
            Files.createDirectories(basePath);
            tempFile = Files.createTempFile(basePath, "upload-", ".tmp");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream inputStream = new DigestInputStream(file.getInputStream(), digest)) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            contentHash = toHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new FileStorageException("Failed to initialize file hashing", exception);
        } catch (IOException exception) {
            throw new FileStorageException("Failed to store file", exception);
        }

        Path targetPath = createDeterministicTargetPath(contentHash, safeFileName);
        if (Files.exists(targetPath)) {
            cleanupTempFile(tempFile);
            throw new FileConflictException("That file is already present. Please upload a new one or a different one.");
        }

        try {
            Files.move(tempFile, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new FileStorageException("Failed to finalize file storage", exception);
        } finally {
            cleanupTempFile(tempFile);
        }

        String contentType = StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        String storedPath = toStoredPath(targetPath);
        return new BusinessFileUploadResponse(storedPath, targetPath.getFileName().toString(), contentType, file.getSize());
    }

    @Override
    public DownloadedBusinessFile getFile(String filePath) {
        Path normalizedFilePath = validateAndNormalizeStoredPath(filePath);

        if (!normalizedFilePath.startsWith(basePath)) {
            throw new InvalidFilePathException("Invalid file path");
        }
        if (!Files.exists(normalizedFilePath) || !Files.isRegularFile(normalizedFilePath)) {
            throw new ResourceNotFoundException("File not found");
        }

        String contentType = detectContentType(normalizedFilePath);
        String fileName = normalizedFilePath.getFileName().toString();
        long size = fileSize(normalizedFilePath);

        return new DownloadedBusinessFile(new FileSystemResource(normalizedFilePath), fileName, contentType, size);
    }

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

    private String extractExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index);
    }

    private Path validateAndNormalizeStoredPath(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new InvalidFilePathException("File path is required");
        }

        Path path = Paths.get(filePath).toAbsolutePath().normalize();
        if (!path.startsWith(basePath)) {
            throw new InvalidFilePathException("Invalid file path");
        }

        return path;
    }

    private String detectContentType(Path filePath) {
        try {
            String contentType = Files.probeContentType(filePath);
            return StringUtils.hasText(contentType)
                    ? contentType
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        } catch (IOException ignored) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    private long fileSize(Path filePath) {
        try {
            return Files.size(filePath);
        } catch (IOException exception) {
            throw new FileStorageException("Failed to read file size", exception);
        }
    }

    private String toStoredPath(Path absolutePath) {
        return absolutePath.toAbsolutePath().normalize().toString();
    }

    private Path createDeterministicTargetPath(String contentHash, String safeFileName) {
        String deterministicFileName = contentHash + extractExtension(safeFileName);
        Path targetPath = basePath.resolve(deterministicFileName).normalize();
        if (!targetPath.startsWith(basePath)) {
            throw new InvalidFilePathException("Invalid upload file path");
        }
        return targetPath;
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }

    private void cleanupTempFile(Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException ignored) {
            // Best-effort cleanup.
        }
    }
}
