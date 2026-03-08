package com.nifilili.business.dto.response;

import com.nifilili.core.enums.business.BusinessStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(
        name = "CreateBusinessResponse",
        description = "Response returned after a draft business is created."
)
public class CreateBusinessResponse {

    @Schema(description = "Identifier of the newly created business.", example = "501")
    private Long businessId;

    @Schema(description = "Current business onboarding status after creation.", example = "DRAFT")
    private BusinessStatus status; // DRAFT

    @Schema(description = "Human-readable confirmation message for the operation.", example = "Business created successfully.")
    private String message;
}
