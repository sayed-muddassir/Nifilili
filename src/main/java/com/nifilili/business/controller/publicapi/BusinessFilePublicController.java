package com.nifilili.business.controller.publicapi;

import com.nifilili.business.service.BusinessFileService;
import com.nifilili.core.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/files")
@RequiredArgsConstructor
@Tag(name = SwaggerConstants.BUSINESS_5)
public class BusinessFilePublicController {

    private final BusinessFileService businessFileService;

    @Operation(
            summary = "Download business file",
            description = "Downloads a file using the exact stored server file path."
    )
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String filePath) {
        BusinessFileService.DownloadedBusinessFile file = businessFileService.getFile(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.size())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.fileName()).build().toString()
                )
                .body(file.resource());
    }
}
