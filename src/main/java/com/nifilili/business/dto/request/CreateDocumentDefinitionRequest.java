package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(
        name = "CreateDocumentDefinitionRequest",
        description = "Payload used by admins to define a business KYC or verification document requirement."
)
public class CreateDocumentDefinitionRequest {

    @Schema(description = "Identifier of the vertical this document requirement applies to.", example = "1", nullable = true)
    private Long verticalId;

    @Schema(description = "Internal document definition name.", example = "business_pan")
    private String name;

    @Schema(description = "Human-readable label shown to users uploading the document.", example = "Business PAN Certificate")
    private String label;

    @ArraySchema(
            arraySchema = @Schema(description = "Allowed file extensions for uploads.", nullable = true),
            schema = @Schema(example = "pdf")
    )
    private List<String> allowedExtensions;

    @Schema(description = "Maximum allowed file size in bytes.", example = "5242880", nullable = true)
    private Integer maxFileSize;

    @Schema(description = "Whether the document must be submitted before business review.", example = "true")
    private boolean required;
}
