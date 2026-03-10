package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(
        name = "SubmitBusinessForReviewRequest",
        description = "Payload used to submit a completed business onboarding flow for admin review."
)
public class SubmitBusinessForReviewRequest {

    @Schema(description = "Note or message submitted together with the review request.", example = "All mandatory KYC documents have been uploaded.")
    @NotBlank
    private String message;
}
