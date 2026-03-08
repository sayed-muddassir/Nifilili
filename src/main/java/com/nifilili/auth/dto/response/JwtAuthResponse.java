package com.nifilili.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "JwtAuthResponse",
        description = "Authentication response containing a bearer access token and the authenticated user's profile."
)
public class JwtAuthResponse {

    @Schema(
            description = "JWT access token to send in the Authorization header as 'Bearer <token>'.",
            example = "eyJhbGciOiJIUzI1NiJ9.example.jwt.token"
    )
    private String accessToken;

    @Schema(
            description = "Authentication scheme prefix expected in the Authorization header.",
            example = "Bearer",
            defaultValue = "Bearer"
    )
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(
            description = "Authenticated user profile returned together with the access token."
    )
    private UserProfileResponse user;
}
