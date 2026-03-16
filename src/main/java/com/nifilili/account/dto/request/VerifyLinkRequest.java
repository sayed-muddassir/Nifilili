package com.nifilili.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "VerifyLinkRequest", description = "Request to complete identity linking by verifying OTP.")
public class VerifyLinkRequest {

    @Schema(description = "The identifier (phone or email) being linked.", example = "+977-9800000000 or jhondoe@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Identifier is required")
    private String identifier;

    @Schema(description = "The 6-digit OTP code.", example = "123456",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "OTP is required")
    private String otp;
}
