package com.nifilili.auth.service;

import com.nifilili.auth.domain.RefreshToken;

import java.util.List;

/**
 * Manages refresh token lifecycle: creation, rotation, revocation, and session listing.
 */
public interface TokenService {

    /**
     * Creates a new refresh token for the user with device tracking info.
     *
     * @param userId    authenticated user ID
     * @param deviceName device label (e.g. "Chrome on MacOS")
     * @param ipAddress  client IP address
     * @param userAgent  browser user-agent string
     * @return the persisted refresh token
     */
    RefreshToken createRefreshToken(Long userId, String deviceName, String ipAddress, String userAgent);

    /**
     * Validates and rotates a refresh token. The old token is revoked and a new one is issued.
     *
     * @param rawToken the refresh token string
     * @return the new refresh token
     * @throws com.nifilili.core.exception.InvalidTokenException if token is invalid, revoked, or expired
     */
    RefreshToken rotateRefreshToken(String rawToken);

    /**
     * Revokes a specific refresh token (logout from one device).
     *
     * @param rawToken the refresh token string
     */
    void revokeRefreshToken(String rawToken);

    /**
     * Revokes all refresh tokens for the user (logout from all devices).
     *
     * @param userId user ID
     */
    void revokeAllRefreshTokens(Long userId);

    /**
     * Lists all active (non-revoked, non-expired) sessions for the user.
     *
     * @param userId user ID
     * @return list of active refresh tokens
     */
    List<RefreshToken> getActiveSessions(Long userId);
}
