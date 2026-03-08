package com.nifilili.business.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(
        name = "DocumentDefinitionResponse",
        description = "Metadata describing a business KYC or verification document requirement."
)
public class DocumentDefinitionResponse {

    @Schema(description = "Unique identifier of the document definition.", example = "55")
    private Long id;

    @Schema(description = "Identifier of the vertical this document definition belongs to.", example = "1")
    private Long verticalId;

    @Schema(description = "Internal document definition name.", example = "business_pan")
    private String name;

    @Schema(description = "Human-readable label shown to users.", example = "Business PAN Certificate")
    private String label;

    @ArraySchema(
            arraySchema = @Schema(description = "Allowed file extensions for uploaded documents.", nullable = true),
            schema = @Schema(example = "pdf")
    )
    private List<String> allowedExtensions;

    @Schema(description = "Maximum permitted upload size in bytes.", example = "5242880", nullable = true)
    private Integer maxFileSize;

    @Schema(description = "Whether the document is required before business review.", example = "true")
    private boolean required;
}
