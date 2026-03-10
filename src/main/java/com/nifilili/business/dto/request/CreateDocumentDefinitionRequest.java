package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(
        name = "CreateDocumentDefinitionRequest",
        description = "Payload used by admins to define a business KYC or verification document requirement."
)
public class CreateDocumentDefinitionRequest {

    @NotNull
    @Schema(description = "Identifier of the vertical this document requirement applies to.", example = "1", nullable = true)
    private Long verticalId;

    @NotBlank
    @Schema(description = "Internal document definition name.", example = "business_pan")
    private String name;

    @NotBlank
    @Schema(description = "Human-readable label shown to users uploading the document.", example = "Business PAN Certificate")
    private String label;

    @NotNull
    @ArraySchema(
            arraySchema = @Schema(description = "Allowed file extensions for uploads."),
            schema = @Schema(example = "pdf")
    )
    private List<String> allowedExtensions;

    @NotNull
    @Schema(description = "Maximum allowed file size in bytes.", example = "5242880")
    private Integer maxFileSize;

    @Schema(description = "Whether the document must be submitted before business review.", example = "true")
    private boolean required;
}
