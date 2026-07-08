package com.nifilili.business.controller.owner;

import com.nifilili.business.controller.BusinessFileController;
import com.nifilili.business.dto.response.BusinessFileUploadResponse;
import com.nifilili.business.service.BusinessFileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessFileControllerTest {

    @Mock
    private BusinessFileService businessFileService;

    @InjectMocks
    private BusinessFileController businessFileController;

    @Test
    void uploadFile_WhenValidRequest_ShouldReturnCreated() {
        MockMultipartFile file = new MockMultipartFile("file", "registration.pdf", "application/pdf", "abc".getBytes());
        BusinessFileUploadResponse payload = new BusinessFileUploadResponse("uuid-registration.pdf", "uuid-registration.pdf", "application/pdf", 3L, "uuid-registration.pdf", 1, "123213qs");
        when(businessFileService.uploadFile(file)).thenReturn(payload);

        ResponseEntity<BusinessFileUploadResponse> response = businessFileController.uploadFile(file);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("uuid-registration.pdf", response.getBody().getRelativePath());
        verify(businessFileService).uploadFile(file);
    }

    @Test
    void downloadFile_WhenValidPath_ShouldReturnBinaryResponseWithHeaders() {
        ByteArrayResource resource = new ByteArrayResource("payload".getBytes());
        BusinessFileService.DownloadedBusinessFile payload =
                new BusinessFileService.DownloadedBusinessFile(resource, "registration.pdf", "application/pdf", 7L);
        when(businessFileService.getFile("uuid-registration.pdf")).thenReturn(payload);

        ResponseEntity<org.springframework.core.io.Resource> response = businessFileController.downloadFile("uuid-registration.pdf");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(7L, response.getHeaders().getContentLength());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("registration.pdf"));
        verify(businessFileService).getFile("uuid-registration.pdf");
    }
}
