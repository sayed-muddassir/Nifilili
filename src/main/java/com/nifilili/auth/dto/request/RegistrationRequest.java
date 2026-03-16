package com.nifilili.auth.dto.request;

import com.nifilili.auth.domain.enums.AuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified registration request supporting email+password and phone+OTP flows.
 * At least one of email or phone must be provided.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "RegistrationRequest",
        description = "Unified registration payload. Provide email+password or phone+OTP based on authType."
)
public class RegistrationRequest {

    @Schema(description = "Authentication method for registration.", example = "EMAIL_PASSWORD",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "authType is required")
    private AuthType authType;

    @Schema(description = "Full display name of the user.", example = "John Doe",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @Schema(description = "Unique username. Auto-generated from name if not provided.", example = "johndoe")
    @Size(min = 3, max = 50)
    private String username;

    @Schema(description = "Email address (required for EMAIL_PASSWORD).", example = "john.doe@nifilili.com",
            format = "email")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Password (required for EMAIL_PASSWORD and PHONE_OTP).", example = "Pass@1234",
            format = "password")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    private String password;

    @Schema(description = "Phone number (required for PHONE_OTP).", example = "+977-9800000000")
    @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid phone number format")
    private String phone;

    @Schema(description = "OTP code (required for PHONE_OTP registration).", example = "123456")
    private String otp;
}
