package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        name = "SubmitBusinessForReviewRequest",
        description = "Payload used to submit a completed business onboarding flow for admin review."
)
public class SubmitBusinessForReviewRequest {

    @Schema(description = "Optional note or message submitted together with the review request.", example = "All mandatory KYC documents have been uploaded.", nullable = true)
    private String message;
}
