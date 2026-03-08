package com.nifilili.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "RegisterRequest",
        description = "Payload used to create a new user account and immediately receive an authenticated session."
)
public class RegisterDto {

    @Schema(
            description = "Full display name of the user.",
            example = "John Doe",
            minLength = 2,
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @Schema(
            description = "Unique username used for login and profile identification.",
            example = "johndoe",
            minLength = 3,
            maxLength = 50,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @Schema(
            description = "Unique email address for the account.",
            example = "john.doe@nifilili.com",
            format = "email",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Email
    private String email;

    @Schema(
            description = "Password for the new account. Must be 8 to 100 characters and include at least one uppercase letter, one lowercase letter, and one digit.",
            example = "Pass@1234",
            minLength = 8,
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "password"
    )
    @NotBlank
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    private String password;

    @Schema(
            description = "Optional contact phone number in international or local numeric format.",
            example = "+977-9800000000",
            nullable = true
    )
    @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid phone number format")
    private String phone;
}
