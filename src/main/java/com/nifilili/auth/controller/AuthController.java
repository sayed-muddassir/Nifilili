package com.nifilili.auth.controller;

import com.nifilili.auth.dto.request.LoginDto;
import com.nifilili.auth.dto.request.RegisterDto;
import com.nifilili.auth.dto.response.JwtAuthResponse;
import com.nifilili.auth.dto.response.UserProfileResponse;
import com.nifilili.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "User Registration",
            description = "Creates a new user account and returns a JWT token with user profile.",
            tags = {"Auth [Public]"}
    )
    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponse> register(@Valid @RequestBody RegisterDto registerDto) {
        log.info("Registration attempt for email '{}'", registerDto.getEmail());
        JwtAuthResponse response = authService.register(registerDto);
        log.info("Registration succeeded for username '{}'", registerDto.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "User Login",
            description = "Authenticates a user and returns a JWT token with user profile.",
            tags = {"Auth [Public]"}
    )
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginDto loginDto) {
        log.info("Login attempt for identifier '{}'", loginDto.getUsernameOrEmail());
        JwtAuthResponse response = authService.login(loginDto);
        log.info("Login succeeded for identifier '{}'", loginDto.getUsernameOrEmail());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Get Current User Profile",
            description = "Returns the authenticated user's profile. Requires a valid Bearer token.",
            tags = {"Auth [Protected]"}
    )
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser() {
        log.debug("Current user profile request");
        UserProfileResponse profile = authService.getCurrentUser();
        return ResponseEntity.ok(profile);
    }
}
