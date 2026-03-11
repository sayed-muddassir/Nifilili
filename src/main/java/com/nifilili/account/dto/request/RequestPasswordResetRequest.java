package com.nifilili.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "RequestPasswordResetRequest", description = "Request to initiate a password reset via email link or OTP.")
public class RequestPasswordResetRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Schema(description = "Email address of the account to reset.", example = "user@example.com")
    private String email;

    @NotBlank(message = "Reset type is required")
    @Pattern(regexp = "^(LINK|OTP)$", message = "Type must be either LINK or OTP")
    @Schema(description = "Reset method: LINK for email link (1h expiry) or OTP for 6-digit code (10min expiry).",
            allowableValues = {"LINK", "OTP"}, example = "LINK")
    private String type;
}
