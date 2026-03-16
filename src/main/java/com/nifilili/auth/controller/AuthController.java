package com.nifilili.auth.controller;

import com.nifilili.auth.dto.request.AuthRequest;
import com.nifilili.auth.dto.request.LoginDto;
import com.nifilili.auth.dto.request.LogoutRequest;
import com.nifilili.auth.dto.request.OtpRequestDto;
import com.nifilili.auth.dto.request.OtpVerifyDto;
import com.nifilili.auth.dto.request.RefreshTokenRequest;
import com.nifilili.auth.dto.request.RegisterDto;
import com.nifilili.auth.dto.request.RegistrationRequest;
import com.nifilili.auth.dto.response.JwtAuthResponse;
import com.nifilili.auth.dto.response.UserProfileResponse;
import com.nifilili.auth.service.AuthService;
import com.nifilili.core.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Unified Registration",
            description = "Registers a new user via the specified auth method (EMAIL_PASSWORD or PHONE_OTP). "
                    + "For PHONE_OTP: request OTP first via /api/auth/otp/request, then include phone+otp+password.",
            tags = {SwaggerConstants.AUTH_1}
    )
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "409", description = "Email, username, or phone already exists")
    @ApiResponse(responseCode = "400", description = "Validation failed or invalid OTP")
    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponse> register(@Valid @RequestBody RegistrationRequest registrationRequest,
                                                      HttpServletRequest request) {
        log.info("V1 registration: authType={}", registrationRequest.getAuthType());
        JwtAuthResponse response = authService.register(
                registrationRequest, getClientIp(request), getUserAgent(request), getDeviceName(request));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Unified Login",
            description = "Authenticates via the specified auth method (EMAIL_PASSWORD or PHONE_OTP). "
                    + "For PHONE_OTP: request OTP first via /api/auth/otp/request.",
            tags = {SwaggerConstants.AUTH_1}
    )
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials or OTP")
    @ApiResponse(responseCode = "423", description = "Account locked")
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody AuthRequest authRequest,
                                                   HttpServletRequest request) {
        log.info("V1 login: authType={}", authRequest.getAuthType());
        JwtAuthResponse response = authService.login(
                authRequest, getClientIp(request), getUserAgent(request), getDeviceName(request));
        return ResponseEntity.ok(response);
    }

    // ── OTP endpoints ───────────────────────────────────────────────────

    @Operation(
            summary = "Request OTP",
            description = "Generates and sends an OTP to the specified phone/email. "
                    + "Rate-limited to 3 requests per 5 minutes per identifier.",
            tags = {SwaggerConstants.AUTH_1}
    )
    @ApiResponse(responseCode = "200", description = "OTP sent successfully")
    @ApiResponse(responseCode = "429", description = "Too many OTP requests")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @PostMapping("/otp/request")
    public ResponseEntity<Map<String, String>> requestOtp(@Valid @RequestBody OtpRequestDto otpRequest) {
        log.info("OTP request: identifier='{}' purpose={}", otpRequest.getIdentifier(), otpRequest.getPurpose());
        authService.requestOtp(otpRequest);
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
    }

    @Operation(
            summary = "Verify OTP and Login",
            description = "Verifies the OTP and returns JWT tokens. The user must have an existing account.",
            tags = {SwaggerConstants.AUTH_1}
    )
    @ApiResponse(responseCode = "200", description = "OTP verified, login successful")
    @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    @ApiResponse(responseCode = "404", description = "No account found for phone number")
    @PostMapping("/otp/verify")
    public ResponseEntity<JwtAuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyDto verifyDto,
                                                     HttpServletRequest request) {
        log.info("OTP verify for phone '{}'", verifyDto.getPhone());
        JwtAuthResponse response = authService.verifyOtpAndLogin(
                verifyDto, getClientIp(request), getUserAgent(request), getDeviceName(request));
        return ResponseEntity.ok(response);
    }

    // ── Token management ────────────────────────────────────────────────

    @Operation(
            summary = "Refresh Access Token",
            description = "Uses a valid refresh token to obtain a new access token. "
                    + "The refresh token is rotated on each use.",
            tags = {SwaggerConstants.AUTH_1}
    )
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        log.debug("Token refresh request");
        JwtAuthResponse response = authService.refresh(refreshRequest.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Logout (Single Device)",
            description = "Revokes the specified refresh token, ending the session on that device.",
            tags = {SwaggerConstants.AUTH_2}
    )
    @ApiResponse(responseCode = "204", description = "Logged out successfully")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        log.info("Logout request");
        authService.logout(logoutRequest.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Logout All Devices",
            description = "Revokes all refresh tokens for the current user, ending all sessions.",
            tags = {SwaggerConstants.AUTH_2}
    )
    @ApiResponse(responseCode = "204", description = "All sessions revoked")
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll() {
        log.info("Logout-all request");
        authService.logoutAll();
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get Current User Profile",
            description = "Returns the authenticated user's profile. Requires a valid Bearer token.",
            tags = {SwaggerConstants.AUTH_2}
    )
    @ApiResponse(responseCode = "200", description = "Profile retrieved")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser() {
        log.debug("Current user profile request");
        UserProfileResponse profile = authService.getCurrentUser();
        return ResponseEntity.ok(profile);
    }

    // ── Request helpers ─────────────────────────────────────────────────

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    private String getDeviceName(HttpServletRequest request) {
        String deviceName = request.getHeader("X-Device-Name");
        if (deviceName != null && !deviceName.isBlank()) {
            return deviceName;
        }
        String ua = getUserAgent(request);
        if (ua != null && ua.length() > 50) {
            return ua.substring(0, 50);
        }
        return ua;
    }
}
