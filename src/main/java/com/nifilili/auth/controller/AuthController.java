package com.nifilili.auth.controller;

import com.nifilili.auth.dto.request.LoginDto;
import com.nifilili.auth.dto.request.LogoutRequest;
import com.nifilili.auth.dto.request.RefreshTokenRequest;
import com.nifilili.auth.dto.request.RegisterDto;
import com.nifilili.auth.dto.response.JwtAuthResponse;
import com.nifilili.auth.dto.response.UserProfileResponse;
import com.nifilili.auth.service.AuthService;
import com.nifilili.core.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "User Registration",
            description = "Creates a new user account and returns access + refresh tokens with user profile. "
                    + "Email verification email is sent automatically.",
            tags = {SwaggerConstants.AUTH_1}
    )
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "409", description = "Email or username already exists")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponse> register(@Valid @RequestBody RegisterDto registerDto,
                                                    HttpServletRequest request) {
        log.info("Registration attempt for email '{}'", registerDto.getEmail());
        JwtAuthResponse response = authService.register(
                registerDto, getClientIp(request), getUserAgent(request), getDeviceName(request));
        log.info("Registration succeeded for username '{}'", registerDto.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "User Login",
            description = "Authenticates a user and returns access + refresh tokens with user profile. "
                    + "Account is locked after 5 consecutive failed attempts.",
            tags = {SwaggerConstants.AUTH_1}
    )
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "423", description = "Account locked")
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginDto loginDto,
                                                 HttpServletRequest request) {
        log.info("Login attempt for identifier '{}'", loginDto.getUsernameOrEmail());
        JwtAuthResponse response = authService.login(
                loginDto, getClientIp(request), getUserAgent(request), getDeviceName(request));
        log.info("Login succeeded for identifier '{}'", loginDto.getUsernameOrEmail());
        return ResponseEntity.ok(response);
    }

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
        // Custom header for device name; falls back to User-Agent summary
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
