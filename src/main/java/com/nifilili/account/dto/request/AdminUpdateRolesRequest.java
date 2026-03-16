package com.nifilili.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AdminUpdateRolesRequest", description = "Request to replace a user's roles entirely.")
public class AdminUpdateRolesRequest {

    @Schema(description = "Complete set of role names to assign. Replaces all existing roles.",
            example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;
}
