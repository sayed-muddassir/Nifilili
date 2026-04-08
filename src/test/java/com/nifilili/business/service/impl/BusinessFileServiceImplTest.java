package com.nifilili.business.service.impl;

import com.nifilili.business.dto.response.BusinessFileUploadResponse;
import com.nifilili.business.service.BusinessFileService;
import com.nifilili.core.exception.FileConflictException;
import com.nifilili.core.exception.InvalidFilePathException;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BusinessFileServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void uploadFile_WhenValidRequest_ShouldStoreFileAndReturnUniqueRelativePath() {
        BusinessFileServiceImpl service = new BusinessFileServiceImpl(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "registration.pdf",
                "application/pdf",
                "sample-content".getBytes()
        );

        BusinessFileUploadResponse response = service.uploadFile(file);

        assertTrue(response.getRelativePath().endsWith(".pdf"));
        assertEquals("application/pdf", response.getContentType());
        assertEquals(file.getSize(), response.getSize());
        assertTrue(Files.exists(Path.of(response.getRelativePath())));
    }

    @Test
    void uploadFile_WhenSameFileUploadedTwice_ShouldThrowConflictException() {
        BusinessFileServiceImpl service = new BusinessFileServiceImpl(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "img-data".getBytes());

        service.uploadFile(file);
        FileConflictException exception = assertThrows(FileConflictException.class, () -> service.uploadFile(file));

        assertEquals("That file is already present. Please upload a new one or a different one.", exception.getMessage());
    }

    @Test
    void uploadFile_WhenNullFile_ShouldThrowBadRequest() {
        BusinessFileServiceImpl service = new BusinessFileServiceImpl(tempDir.toString());

        assertThrows(IllegalArgumentException.class, () -> service.uploadFile(null));
    }

    @Test
    void uploadFile_WhenContentDiffers_ShouldStoreAtDifferentPaths() {
        BusinessFileServiceImpl service = new BusinessFileServiceImpl(tempDir.toString());
        MockMultipartFile firstFile = new MockMultipartFile("file", "same-name.pdf", "application/pdf", "v1".getBytes());
        MockMultipartFile secondFile = new MockMultipartFile("file", "same-name.pdf", "application/pdf", "v2".getBytes());

        BusinessFileUploadResponse first = service.uploadFile(firstFile);
        BusinessFileUploadResponse second = service.uploadFile(secondFile);

        assertNotEquals(first.getRelativePath(), second.getRelativePath());
        assertTrue(Files.exists(Path.of(first.getRelativePath())));
        assertTrue(Files.exists(Path.of(second.getRelativePath())));
    }

    @Test
    void getFile_WhenPathValid_ShouldReturnResource() throws IOException {
        BusinessFileServiceImpl service = new BusinessFileServiceImpl(tempDir.toString());

        Path filePath = tempDir.resolve("abc-123.pdf");
        Files.writeString(filePath, "payload");

        BusinessFileService.DownloadedBusinessFile downloaded = service.getFile(filePath.toString());

        assertEquals("abc-123.pdf", downloaded.fileName());
        assertEquals(Files.size(filePath), downloaded.size());
        assertTrue(downloaded.resource().exists());
    }

    @Test
    void getFile_WhenPathContainsTraversal_ShouldThrowInvalidPath() {
        BusinessFileServiceImpl service = new BusinessFileServiceImpl(tempDir.toString());

        assertThrows(InvalidFilePathException.class, () -> service.getFile("/etc/passwd"));
    }

    @Test
    void getFile_WhenPathStructureIsMalformed_ShouldThrowInvalidPath() {
        BusinessFileServiceImpl service = new BusinessFileServiceImpl(tempDir.toString());

        assertThrows(InvalidFilePathException.class, () -> service.getFile("folder/file.pdf"));
    }

    @Test
    void getFile_WhenFileDoesNotExist_ShouldThrowNotFound() {
        BusinessFileServiceImpl service = new BusinessFileServiceImpl(tempDir.toString());

        assertThrows(ResourceNotFoundException.class, () -> service.getFile(tempDir.resolve("missing.pdf").toString()));
    }
}
