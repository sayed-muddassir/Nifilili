package com.nifilili.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "VerifyEmailRequest", description = "Request to verify an email address using a verification token.")
public class VerifyEmailRequest {

    @NotBlank(message = "Verification token is required")
    @Schema(description = "The email verification token from the verification link.")
    private String token;
}
