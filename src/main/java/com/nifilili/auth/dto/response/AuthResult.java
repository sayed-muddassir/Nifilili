package com.nifilili.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Internal result from an {@code AuthProviderStrategy} operation.
 * Contains the authenticated user's ID and username, used by the
 * auth service to generate JWT tokens.
 */
@Getter
@Builder
@AllArgsConstructor
public class AuthResult {

    private final Long userId;
    private final String username;
}
