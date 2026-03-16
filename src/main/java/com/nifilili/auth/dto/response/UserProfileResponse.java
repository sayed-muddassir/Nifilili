package com.nifilili.auth.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "UserProfileResponse",
        description = "Authenticated user profile data exposed to the client after login, registration, or profile lookup."
)
public class UserProfileResponse {

    @Schema(description = "Unique database identifier of the user.", example = "101")
    private Long id;

    @Schema(description = "Full display name of the user.", example = "John Doe")
    private String name;

    @Schema(description = "Unique username of the user.", example = "johndoe")
    private String username;

    @Schema(description = "Registered email address of the user.", example = "john.doe@nifilili.com")
    private String email;

    @Schema(description = "Optional phone number configured on the profile.", example = "+977-9800000000", nullable = true)
    private String phone;

    @Schema(description = "Whether the account is currently enabled for login and access.", example = "true")
    private boolean enabled;

    @Schema(description = "Whether the user's email address has been verified.", example = "false")
    private boolean emailVerified;

    @Schema(description = "Whether the user's phone number has been verified.", example = "false")
    private boolean phoneVerified;

    @ArraySchema(
            schema = @Schema(description = "Role granted to the user.", example = "ROLE_USER"),
            arraySchema = @Schema(description = "Set of security roles assigned to the user.")
    )
    private Set<String> roles;
}
