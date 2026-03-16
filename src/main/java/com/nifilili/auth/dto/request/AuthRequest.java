package com.nifilili.auth.dto.request;

import com.nifilili.auth.domain.enums.AuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified authentication request supporting multiple auth methods.
 * Only the fields relevant to the chosen {@code authType} need to be populated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "AuthRequest",
        description = "Unified login request. Populate fields based on the chosen authType."
)
public class AuthRequest {

    @Schema(
            description = "Authentication method to use.",
            example = "EMAIL_PASSWORD",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "authType is required")
    private AuthType authType;

    @Schema(description = "Username or email (for EMAIL_PASSWORD login).", example = "johndoe")
    private String usernameOrEmail;

    @Schema(description = "Password (for EMAIL_PASSWORD login).", example = "Pass@1234", format = "password")
    private String password;

    @Schema(description = "Phone number (for PHONE_OTP login).", example = "+977-9800000000")
    private String phone;

    @Schema(description = "OTP code (for PHONE_OTP login).", example = "123456")
    private String otp;
}
