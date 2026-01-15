package com.nifilili.auth.controller;

import com.nifilili.auth.dto.request.LoginDto;
import com.nifilili.auth.dto.response.JwtAuthResponse;
import com.nifilili.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthService authService;

    // Register
    // Login
    // Logout
    // Refresh Token
    // Get Current User

    // Build Login REST API
    @Operation(summary = "User Login",
            description = "Authenticates a user and returns a JWT token upon successful login.",
            tags = {"Login [Public]"}
    )
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginDto) {
        String token = authService.login(loginDto);

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);

        return new ResponseEntity<>(jwtAuthResponse, HttpStatus.OK);
    }

}
