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

    @Schema(description = "Exact stored file path on server.", example = "/tmp/nifilili/business-files/abc123.pdf")
    private String relativePath;

    @Schema(description = "Stored file name.", example = "registration.pdf")
    private String fileName;

    @Schema(description = "Detected or provided content type.", example = "application/pdf")
    private String contentType;

    @Schema(description = "Stored file size in bytes.", example = "204800")
    private Long size;
}
