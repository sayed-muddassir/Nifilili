package com.nifilili.auth.dto.request;

import com.nifilili.auth.domain.enums.IdentifierType;
import com.nifilili.auth.domain.enums.OtpPurpose;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to generate and send an OTP.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OtpRequest", description = "Request to generate and send an OTP to a phone or email.")
public class OtpRequestDto {

    @Schema(description = "Phone number or email to send OTP to.", example = "+977-9800000000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Identifier is required")
    private String identifier;

    @Schema(description = "Type of identifier: PHONE or EMAIL.", example = "PHONE",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Identifier type is required")
    private IdentifierType identifierType;

    @Schema(description = "Purpose for the OTP.", example = "LOGIN",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Purpose is required")
    private OtpPurpose purpose;
}
