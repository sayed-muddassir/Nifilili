package com.nifilili.auth.dto.request;

import com.nifilili.auth.domain.enums.OtpPurpose;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to verify an OTP for login purposes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OtpVerifyRequest", description = "Verify an OTP to complete login.")
public class OtpVerifyDto {

    @Schema(description = "Phone number or email the OTP was sent to.", example = "+977-9800000000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Phone number is required")
    private String phone;

    @Schema(description = "The 6-digit OTP code.", example = "123456",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "OTP is required")
    private String otp;

    @Schema(description = "Purpose of the OTP.", example = "LOGIN",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Purpose is required")
    private OtpPurpose purpose;
}
