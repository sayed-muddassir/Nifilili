package com.nifilili.auth.service;

import com.nifilili.auth.dto.request.LoginDto;
import com.nifilili.auth.dto.request.RegisterDto;
import com.nifilili.auth.dto.response.JwtAuthResponse;
import com.nifilili.auth.dto.response.UserProfileResponse;

public interface AuthService {

    /**
     * Authenticates a user and returns access + refresh tokens.
     * Checks account lockout before authenticating.
     *
     * @param loginDto login credentials
     * @param ipAddress client IP address
     * @param userAgent client user-agent string
     * @param deviceName device label
     * @return JWT auth response with dual tokens
     * @throws com.nifilili.core.exception.AccountLockedException if the account is locked
     */
    JwtAuthResponse login(LoginDto loginDto, String ipAddress, String userAgent, String deviceName);

    /**
     * Registers a new user and returns access + refresh tokens.
     * Publishes a UserRegisteredEvent for email verification.
     *
     * @param registerDto registration data
     * @param ipAddress client IP address
     * @param userAgent client user-agent string
     * @param deviceName device label
     * @return JWT auth response with dual tokens
     */
    JwtAuthResponse register(RegisterDto registerDto, String ipAddress, String userAgent, String deviceName);

    /**
     * Refreshes the access token using a valid refresh token.
     * Rotates the refresh token on each use.
     *
     * @param refreshToken the current refresh token string
     * @return new JWT auth response with rotated tokens
     */
    JwtAuthResponse refresh(String refreshToken);

    /**
     * Revokes a specific refresh token (single-device logout).
     *
     * @param refreshToken the refresh token to revoke
     */
    void logout(String refreshToken);

    /**
     * Revokes all refresh tokens for the current user (all-device logout).
     */
    void logoutAll();

    /**
     * Returns the current authenticated user's profile.
     *
     * @return user profile response
     */
    UserProfileResponse getCurrentUser();
}
