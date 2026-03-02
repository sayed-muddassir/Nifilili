package com.nifilili.auth.dto;

import com.nifilili.auth.dto.request.LoginDto;
import com.nifilili.auth.dto.response.JwtAuthResponse;
import com.nifilili.auth.dto.response.UserProfileResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void loginDto_WithValidValues_ShouldHaveNoValidationErrors() {
        LoginDto dto = new LoginDto("john", "password");

        Set<ConstraintViolation<LoginDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void loginDto_WithBlankValues_ShouldFailValidation() {
        LoginDto dto = new LoginDto("   ", "");

        Set<ConstraintViolation<LoginDto>> violations = validator.validate(dto);

        assertEquals(2, violations.size());
    }

    @Test
    void jwtAuthResponse_ConstructorsAndSetters_ShouldWork() {
        JwtAuthResponse response = new JwtAuthResponse();
        response.setAccessToken("abc");

        assertEquals("abc", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());

        JwtAuthResponse fromAllArgs = new JwtAuthResponse("xyz", "Bearer", new UserProfileResponse());
        assertEquals("xyz", fromAllArgs.getAccessToken());
        assertEquals("Bearer", fromAllArgs.getTokenType());
    }
}

