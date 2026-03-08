package com.nifilili.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "LoginRequest",
        description = "Payload used to authenticate an existing user with either username or email."
)
public class LoginDto {

    @Schema(
            description = "Unique login identifier. Accepts either the user's username or registered email address.",
            example = "johndoe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    private String usernameOrEmail;

    @Schema(
            description = "Plain-text password for the account being authenticated.",
            example = "Pass@1234",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "password"
    )
    @NotBlank
    private String password;
}
