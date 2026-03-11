package com.nifilili.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "LogoutRequest", description = "Request to revoke a specific refresh token (single-device logout).")
public class LogoutRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "The refresh token to revoke.")
    private String refreshToken;
}
