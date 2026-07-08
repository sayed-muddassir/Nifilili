package com.nifilili.business.service.storage;

import com.nifilili.business.service.BusinessFileService.DownloadedBusinessFile;
import com.nifilili.core.exception.InvalidFilePathException;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class LocalFileStorageProviderTest {

    private LocalFileStorageProvider storageProvider;
    private Path tempDirectory;
    private StorageConfiguration storageConfiguration;

    @Mock
    private PathStrategy pathStrategy;

    @BeforeEach
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("file-storage-test-");
        storageConfiguration = new StorageConfiguration();
        storageConfiguration.setBasePath(tempDirectory.toString());
        storageConfiguration.setAllowedExtensions(".pdf,.doc,.xlsx");
        storageConfiguration.setMaxFileSize(10L * 1024L * 1024L); // 10MB

        storageProvider = new LocalFileStorageProvider(new FlatPathStrategy(), storageConfiguration);
        ReflectionTestUtils.setField(storageProvider, "basePath", tempDirectory.toString());
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.walk(tempDirectory)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException ignored) {
                    }
                });
    }

    @Test
    public void uploadFile_WithValidFile_UploadSuccessfully() {
        byte[] content = "test file content".getBytes();
        MultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", content);

        FileStorageProvider.UploadedFileInfo result = storageProvider.uploadFile(file);

        assertNotNull(result);
        assertEquals("document.pdf", result.fileName());
        assertEquals("application/pdf", result.contentType());
        assertEquals(content.length, result.size());
        assertEquals(0, result.version());
        assertNotNull(result.contentHash());
        assertEquals("document.pdf", result.originalFileName());
        assertTrue(result.relativePath().endsWith("document.pdf"));
    }

    @Test
    public void uploadFile_WithNullFile_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> storageProvider.uploadFile(null));
        assertEquals("File is required", exception.getMessage());
    }

    @Test
    public void uploadFile_WithDisallowedExtension_ThrowsException() {
        byte[] content = "test".getBytes();
        MultipartFile file = new MockMultipartFile("file", "malware.exe", "application/octet-stream", content);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> storageProvider.uploadFile(file));
        assertTrue(exception.getMessage().contains("File extension not allowed"));
    }

    @Test
    public void uploadFile_WithExceededFileSize_ThrowsException() {
        byte[] content = new byte[11 * 1024 * 1024]; // 11MB
        MultipartFile file = new MockMultipartFile("file", "large.pdf", "application/pdf", content);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> storageProvider.uploadFile(file));
        assertTrue(exception.getMessage().contains("File size exceeds maximum"));
    }

    @Test
    public void uploadFile_WithDuplicateFile_CreatesVersionedName() throws IOException {
        byte[] content1 = "first upload".getBytes();
        MultipartFile file1 = new MockMultipartFile("file", "report.pdf", "application/pdf", content1);

        FileStorageProvider.UploadedFileInfo result1 = storageProvider.uploadFile(file1);
        assertEquals(0, result1.version());

        byte[] content2 = "second upload".getBytes();
        MultipartFile file2 = new MockMultipartFile("file", "report.pdf", "application/pdf", content2);

        FileStorageProvider.UploadedFileInfo result2 = storageProvider.uploadFile(file2);
        assertEquals(1, result2.version());
        assertTrue(result2.fileName().contains("_v1"));

        // Verify both files exist
        assertTrue(storageProvider.fileExists(result1.relativePath()));
        assertTrue(storageProvider.fileExists(result2.relativePath()));
    }

    @Test
    public void uploadFile_GeneratesCorrectContentHash() {
        byte[] content = "test content".getBytes();
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", content);

        FileStorageProvider.UploadedFileInfo result = storageProvider.uploadFile(file);

        assertNotNull(result.contentHash());
        assertEquals(64, result.contentHash().length()); // SHA-256 hex is 64 characters
    }

    @Test
    public void uploadFile_DetectsContentType() {
        byte[] content = "PDF content".getBytes();
        MultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", content);

        FileStorageProvider.UploadedFileInfo result = storageProvider.uploadFile(file);

        assertEquals("application/pdf", result.contentType());
    }

    @Test
    public void downloadFile_WithValidPath_ReturnsDownloadedFile() throws IOException {
        byte[] content = "download test".getBytes();
        MultipartFile file = new MockMultipartFile("file", "download.pdf", "application/pdf", content);

        FileStorageProvider.UploadedFileInfo uploaded = storageProvider.uploadFile(file);

        DownloadedBusinessFile downloaded = storageProvider.downloadFile(uploaded.relativePath());

        assertEquals("download.pdf", downloaded.fileName());
        assertEquals("application/pdf", downloaded.contentType());
        assertEquals(content.length, downloaded.size());
    }

    @Test
    public void downloadFile_WithInvalidPath_ThrowsException() {
        InvalidFilePathException exception = assertThrows(InvalidFilePathException.class,
                () -> storageProvider.downloadFile(""));
        assertEquals("File path is required", exception.getMessage());
    }

    @Test
    public void downloadFile_WithPathTraversalAttempt_ThrowsException() {
        assertThrows(InvalidFilePathException.class,
                () -> storageProvider.downloadFile("../../../etc/passwd"));
    }

    @Test
    public void downloadFile_WithNonExistentFile_ThrowsException() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> storageProvider.downloadFile("nonexistent.pdf"));
        assertEquals("File not found", exception.getMessage());
    }

    @Test
    public void deleteFile_WithValidPath_DeletesFile() throws IOException {
        byte[] content = "delete test".getBytes();
        MultipartFile file = new MockMultipartFile("file", "delete.pdf", "application/pdf", content);

        FileStorageProvider.UploadedFileInfo uploaded = storageProvider.uploadFile(file);
        assertTrue(storageProvider.fileExists(uploaded.relativePath()));

        storageProvider.deleteFile(uploaded.relativePath());

        assertFalse(storageProvider.fileExists(uploaded.relativePath()));
    }

    @Test
    public void deleteFile_WithNonExistentFile_ThrowsException() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> storageProvider.deleteFile("nonexistent.pdf"));
        assertEquals("File not found", exception.getMessage());
    }

    @Test
    public void fileExists_WithExistingFile_ReturnsTrue() throws IOException {
        byte[] content = "exists test".getBytes();
        MultipartFile file = new MockMultipartFile("file", "exists.pdf", "application/pdf", content);

        FileStorageProvider.UploadedFileInfo uploaded = storageProvider.uploadFile(file);

        assertTrue(storageProvider.fileExists(uploaded.relativePath()));
    }

    @Test
    public void fileExists_WithNonExistentFile_ReturnsFalse() {
        assertFalse(storageProvider.fileExists("nonexistent.pdf"));
    }

    @Test
    public void uploadFile_WithSpecialCharactersInFileName_SanitizesName() {
        byte[] content = "test".getBytes();
        MultipartFile file = new MockMultipartFile("file", "malicious.pdf", "application/pdf", content);

        FileStorageProvider.UploadedFileInfo result = storageProvider.uploadFile(file);

        assertNotNull(result.fileName());
        assertFalse(result.fileName().contains("../"));
    }

    @Test
    public void uploadFile_TemporaryFileIsCleanedUp() throws IOException {
        long tempFileCountBefore = countTempFiles();

        byte[] content = "cleanup test".getBytes();
        MultipartFile file = new MockMultipartFile("file", "cleanup.pdf", "application/pdf", content);
        storageProvider.uploadFile(file);

        long tempFileCountAfter = countTempFiles();

        assertEquals(tempFileCountBefore, tempFileCountAfter, "Temp files should be cleaned up");
    }

    private long countTempFiles() throws IOException {
        return Files.list(tempDirectory)
                .filter(p -> p.getFileName().toString().startsWith("upload-") && p.getFileName().toString().endsWith(".tmp"))
                .count();
    }

    @Test
    public void uploadFile_MultipleVersions_IncrementCorrectly() throws IOException {
        byte[] content = "version test".getBytes();

        for (int i = 0; i < 5; i++) {
            MultipartFile file = new MockMultipartFile("file", "multi.pdf", "application/pdf", content);
            FileStorageProvider.UploadedFileInfo result = storageProvider.uploadFile(file);
            assertEquals(i, result.version());
        }
    }
}
