package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(
        name = "UploadBusinessDocumentRequest",
        description = "Payload used to attach an uploaded business document to a document definition."
)
public class UploadBusinessDocumentRequest {

    @Schema(description = "Identifier of the document definition being fulfilled.", example = "55")
    @NotNull
    private Long documentDefinitionId;

    @Schema(description = "Accessible URL of the uploaded document file.", example = "https://cdn.nifilili.com/documents/business-pan.pdf")
    @NotBlank
    private String fileUrl;

    @Schema(description = "Original or display file name for the uploaded document.", example = "business-pan.pdf")
    @NotBlank
    private String fileName;
}
