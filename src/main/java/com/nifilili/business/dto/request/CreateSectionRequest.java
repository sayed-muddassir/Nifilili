package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        name = "CreateSectionRequest",
        description = "Payload used by admins to create or update a reusable business onboarding section."
)
public class CreateSectionRequest {

    @Schema(description = "Identifier of the vertical the section belongs to.", example = "1", nullable = true)
    private Long verticalId;

    @Schema(description = "Optional category identifier when the section applies only to a specific category.", example = "12", nullable = true)
    private Long categoryId;

    @Schema(description = "Internal section name.", example = "business_identity")
    private String name;

    @Schema(description = "Human-readable label shown in the UI.", example = "Business Identity")
    private String label;

    @Schema(description = "Legacy prompt field retained for backward compatibility.", example = "Tell us about your business identity details.", nullable = true)
    private String prompt;

    @Schema(description = "Prompt text shown to guide the user while filling the section.", example = "Provide your official registration and tax details.", nullable = true)
    private String promptText;

    @Schema(description = "Whether this section must be completed before submission.", example = "true")
    private boolean required;

    @Schema(description = "Whether the section can have multiple entries or groups.", example = "false")
    private boolean allowMultiple;

    @Schema(description = "Whether this section supports grouped, repeatable data blocks.", example = "false")
    private boolean groupable;
}
