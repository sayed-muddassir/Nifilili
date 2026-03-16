package com.nifilili.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "LinkPhoneRequest", description = "Request to initiate phone number linking.")
public class LinkPhoneRequest {

    @Schema(description = "Phone number to link to the account.", example = "+977-9800000000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid phone number format")
    private String phone;
}
