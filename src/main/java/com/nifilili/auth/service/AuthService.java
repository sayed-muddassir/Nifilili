package com.nifilili.auth.service;

import com.nifilili.auth.dto.request.AuthRequest;
import com.nifilili.auth.dto.request.LoginDto;
import com.nifilili.auth.dto.request.OtpRequestDto;
import com.nifilili.auth.dto.request.OtpVerifyDto;
import com.nifilili.auth.dto.request.RegisterDto;
import com.nifilili.auth.dto.request.RegistrationRequest;
import com.nifilili.auth.dto.response.JwtAuthResponse;
import com.nifilili.auth.dto.response.UserProfileResponse;

public interface AuthService {

    /**
     * Authenticates a user via the unified auth request (strategy-based).
     * Routes to the appropriate provider based on {@code authType}.
     *
     * @param request    unified auth request
     * @param ipAddress  client IP address
     * @param userAgent  client user-agent string
     * @param deviceName device label
     * @return JWT auth response with dual tokens
     */
    JwtAuthResponse login(AuthRequest request, String ipAddress, String userAgent, String deviceName);


    /**
     * Registers a new user via the unified registration request (strategy-based).
     *
     * @param request    unified registration data
     * @param ipAddress  client IP address
     * @param userAgent  client user-agent string
     * @param deviceName device label
     * @return JWT auth response with dual tokens
     */
    JwtAuthResponse register(RegistrationRequest request, String ipAddress, String userAgent, String deviceName);


    /**
     * Refreshes the access token using a valid refresh token.
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

    /**
     * Requests an OTP to be generated and sent for the specified purpose.
     *
     * @param otpRequest OTP generation parameters
     */
    void requestOtp(OtpRequestDto otpRequest);

    /**
     * Verifies an OTP and returns JWT tokens (for phone-based login).
     *
     * @param verifyDto  OTP verification data
     * @param ipAddress  client IP address
     * @param userAgent  client user-agent string
     * @param deviceName device label
     * @return JWT auth response with dual tokens
     */
    JwtAuthResponse verifyOtpAndLogin(OtpVerifyDto verifyDto, String ipAddress, String userAgent, String deviceName);
}
