package com.nifilili.auth.service.impl;

import com.nifilili.auth.config.JwtTokenProvider;
import com.nifilili.auth.domain.RefreshToken;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.domain.enums.AuthType;
import com.nifilili.auth.domain.enums.OtpPurpose;
import com.nifilili.auth.dto.request.AuthRequest;
import com.nifilili.auth.dto.request.LoginDto;
import com.nifilili.auth.dto.request.OtpRequestDto;
import com.nifilili.auth.dto.request.OtpVerifyDto;
import com.nifilili.auth.dto.request.RegisterDto;
import com.nifilili.auth.dto.request.RegistrationRequest;
import com.nifilili.auth.dto.response.AuthResult;
import com.nifilili.auth.dto.response.JwtAuthResponse;
import com.nifilili.auth.dto.response.UserProfileResponse;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.auth.service.AuthService;
import com.nifilili.auth.service.OtpService;
import com.nifilili.auth.service.TokenService;
import com.nifilili.core.exception.InvalidTokenException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Orchestrates authentication flows by delegating to the strategy-based
 * {@link AuthProviderRouter} and handling token generation centrally.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final AuthProviderRouter authProviderRouter;
    private final OtpService otpService;

    // ── Unified login (strategy-based) ──────────────────────────────────

    @Override
    @Transactional
    public JwtAuthResponse login(AuthRequest request, String ipAddress, String userAgent, String deviceName) {
        log.debug("Unified login: authType={}", request.getAuthType());

        AuthResult result = authProviderRouter.authenticate(request, ipAddress, userAgent, deviceName);
        return buildJwtResponse(result.getUserId(), result.getUsername(), deviceName, ipAddress, userAgent);
    }

    // ── Unified registration (strategy-based) ───────────────────────────

    @Override
    @Transactional
    public JwtAuthResponse register(RegistrationRequest request, String ipAddress, String userAgent, String deviceName) {
        log.debug("Unified registration: authType={}", request.getAuthType());

        AuthResult result = authProviderRouter.register(request, ipAddress, userAgent, deviceName);
        return buildJwtResponse(result.getUserId(), result.getUsername(), deviceName, ipAddress, userAgent);
    }

    // ── OTP endpoints ───────────────────────────────────────────────────

    @Override
    @Transactional
    public void requestOtp(OtpRequestDto otpRequest) {
        log.debug("OTP request: identifier='{}' purpose={}", otpRequest.getIdentifier(), otpRequest.getPurpose());

        // For SIGNUP purpose, check if phone/email is already registered
        if (otpRequest.getPurpose() == OtpPurpose.SIGNUP) {
            validateIdentifierNotRegistered(otpRequest);
        }

        // For LOGIN purpose, check if account exists
        if (otpRequest.getPurpose() == OtpPurpose.LOGIN) {
            validateIdentifierRegistered(otpRequest);
        }

        otpService.generateAndSendOtp(
                otpRequest.getIdentifier(), otpRequest.getIdentifierType(), otpRequest.getPurpose());
    }

    @Override
    @Transactional
    public JwtAuthResponse verifyOtpAndLogin(OtpVerifyDto verifyDto, String ipAddress, String userAgent,
                                             String deviceName) {
        log.debug("OTP verify-and-login for phone='{}'", verifyDto.getPhone());

        AuthRequest authRequest = new AuthRequest();
        authRequest.setAuthType(AuthType.PHONE_OTP);
        authRequest.setPhone(verifyDto.getPhone());
        authRequest.setOtp(verifyDto.getOtp());

        AuthResult result = authProviderRouter.authenticate(authRequest, ipAddress, userAgent, deviceName);
        return buildJwtResponse(result.getUserId(), result.getUsername(), deviceName, ipAddress, userAgent);
    }

    // ── Token management ────────────────────────────────────────────────

    @Override
    @Transactional
    public JwtAuthResponse refresh(String refreshTokenStr) {
        RefreshToken newRefreshToken = tokenService.rotateRefreshToken(refreshTokenStr);
        User user = userRepository.findById(newRefreshToken.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found for refresh token"));

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        log.debug("Refreshed access token for userId={}", user.getId());

        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .refreshToken(newRefreshToken.getToken())
                .user(buildProfileResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
    }

    @Override
    @Transactional
    public void logoutAll() {
        Long userId = SecurityUtil.getCurrentUserId();
        tokenService.revokeAllRefreshTokens(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in repository"));
        return buildProfileResponse(user);
    }

    // ── Private helpers ─────────────────────────────────────────────────

    private JwtAuthResponse buildJwtResponse(Long userId, String username,
                                             String deviceName, String ipAddress, String userAgent) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId, username);
        RefreshToken refreshToken = tokenService.createRefreshToken(userId, deviceName, ipAddress, userAgent);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .refreshToken(refreshToken.getToken())
                .user(buildProfileResponse(user))
                .build();
    }

    private UserProfileResponse buildProfileResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .roles(roleNames)
                .build();
    }

    private void validateIdentifierNotRegistered(OtpRequestDto otpRequest) {
        switch (otpRequest.getIdentifierType()) {
            case PHONE -> {
                if (userRepository.existsByPhone(otpRequest.getIdentifier())) {
                    throw new InvalidTokenException(
                            "This phone number is already associated with an account. Please login instead.");
                }
            }
            case EMAIL -> {
                if (userRepository.existsByEmail(otpRequest.getIdentifier())) {
                    throw new InvalidTokenException(
                            "This email is already associated with an account. Please login instead.");
                }
            }
        }
    }

    private void validateIdentifierRegistered(OtpRequestDto otpRequest) {
        switch (otpRequest.getIdentifierType()) {
            case PHONE -> {
                if (!userRepository.existsByPhone(otpRequest.getIdentifier())) {
                    throw new ResourceNotFoundException(
                            "No account found for this phone number. Please register first.");
                }
            }
            case EMAIL -> {
                if (!userRepository.existsByEmail(otpRequest.getIdentifier())) {
                    // Silent — same as password reset (anti-enumeration)
                    log.debug("OTP login requested for non-existent email, silently ignoring");
                }
            }
        }
    }
}
