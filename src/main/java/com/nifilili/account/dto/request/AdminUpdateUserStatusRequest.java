package com.nifilili.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AdminUpdateUserStatusRequest", description = "Request to enable or disable a user account.")
public class AdminUpdateUserStatusRequest {

    @Schema(description = "Whether the account should be enabled (true) or disabled (false).",
            example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
}
