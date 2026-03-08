package com.nifilili.business.dto.response;

import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.enums.kyc.KycStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Schema(
        name = "SubmitBusinessResponse",
        description = "Response returned after a business is submitted for review."
)
public class SubmitBusinessResponse {

    @Schema(description = "Identifier of the submitted business.", example = "501")
    private Long businessId;

    @Schema(description = "Business lifecycle status after submission.", example = "PENDING")
    private BusinessStatus status; // PENDING

    @Schema(description = "Current KYC review status after submission.", example = "PENDING")
    private KycStatus kycStatus;

    @Schema(description = "Human-readable confirmation message for the submission.", example = "Business submitted for review successfully.")
    private String message;
}
