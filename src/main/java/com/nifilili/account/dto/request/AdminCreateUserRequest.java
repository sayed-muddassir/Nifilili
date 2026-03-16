package com.nifilili.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AdminCreateUserRequest", description = "Request payload for admin-initiated user creation.")
public class AdminCreateUserRequest {

    @Schema(description = "Full name of the user.", example = "Jane Doe",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Email address. At least one of email or phone must be provided.",
            example = "jane.doe@nifilili.com")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Phone number. At least one of email or phone must be provided.",
            example = "+977-9800000000")
    @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid phone number format")
    private String phone;

    @Schema(description = "Temporary password for the new user.", example = "TempPass@123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @Schema(description = "Set of role names to assign to the user.", example = "[\"ROLE_USER\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;
}
