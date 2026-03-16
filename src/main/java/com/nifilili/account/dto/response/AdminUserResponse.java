package com.nifilili.account.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AdminUserResponse", description = "Detailed user information for admin management.")
public class AdminUserResponse {

    @Schema(description = "Unique user identifier.", example = "101")
    private Long id;

    @Schema(description = "Full name of the user.", example = "Jane Doe")
    private String name;

    @Schema(description = "Username.", example = "janedoe")
    private String username;

    @Schema(description = "Email address.", example = "jane.doe@nifilili.com")
    private String email;

    @Schema(description = "Phone number.", example = "+977-9800000000")
    private String phone;

    @Schema(description = "Whether the account is active.", example = "true")
    private boolean enabled;

    @Schema(description = "Whether the email has been verified.", example = "true")
    private boolean emailVerified;

    @Schema(description = "Whether the phone has been verified.", example = "false")
    private boolean phoneVerified;

    @Schema(description = "Whether the account is locked due to failed login attempts.", example = "false")
    private boolean accountLocked;

    @ArraySchema(
            schema = @Schema(description = "Role assigned to the user.", example = "ROLE_USER"),
            arraySchema = @Schema(description = "Set of roles assigned to the user.")
    )
    private Set<String> roles;

    @Schema(description = "Account creation timestamp.", example = "2026-03-10T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp.", example = "2026-03-10T14:30:00")
    private LocalDateTime updatedAt;
}
