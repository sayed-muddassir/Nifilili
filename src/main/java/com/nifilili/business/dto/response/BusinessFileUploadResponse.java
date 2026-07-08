package com.nifilili.business.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "BusinessFileUploadResponse",
        description = "Metadata returned after uploading a business file to local storage."
)
public class BusinessFileUploadResponse {

    @Schema(description = "Relative path after basePath on server.", example = "registration.pdf")
    private String relativePath;

    @Schema(description = "Stored file name (may include version suffix).", example = "registration_v1.pdf")
    private String fileName;

    @Schema(description = "Detected or provided content type.", example = "application/pdf")
    private String contentType;

    @Schema(description = "Stored file size in bytes.", example = "204800")
    private Long size;

    @Schema(description = "Original filename as uploaded.", example = "registration.pdf")
    private String originalFileName;

    @Schema(description = "Version number (0 for first upload, incremented for duplicates).", example = "0")
    private Integer version;

    @Schema(description = "SHA-256 hash of file content for duplicate detection.", example = "abc123def456...")
    private String contentHash;
}
