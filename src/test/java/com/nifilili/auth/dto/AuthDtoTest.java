package com.nifilili.auth.dto;

import com.nifilili.auth.domain.enums.AuthType;
import com.nifilili.auth.domain.enums.IdentifierType;
import com.nifilili.auth.domain.enums.OtpPurpose;
import com.nifilili.auth.dto.request.AuthRequest;
import com.nifilili.auth.dto.request.LoginDto;
import com.nifilili.auth.dto.request.LogoutRequest;
import com.nifilili.auth.dto.request.OtpRequestDto;
import com.nifilili.auth.dto.request.OtpVerifyDto;
import com.nifilili.auth.dto.request.RefreshTokenRequest;
import com.nifilili.auth.dto.request.RegisterDto;
import com.nifilili.auth.dto.request.RegistrationRequest;
import com.nifilili.auth.dto.response.AuthResult;
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

    // ── LoginDto ──────────────────────────────────────────────────────────

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

    // ── JwtAuthResponse ───────────────────────────────────────────────────

    @Test
    void jwtAuthResponse_ConstructorsAndSetters_ShouldWork() {
        JwtAuthResponse response = new JwtAuthResponse();
        response.setAccessToken("abc");

        assertEquals("abc", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());

        JwtAuthResponse fromAllArgs = new JwtAuthResponse("xyz", "Bearer", "xyz", new UserProfileResponse());
        assertEquals("xyz", fromAllArgs.getAccessToken());
        assertEquals("Bearer", fromAllArgs.getTokenType());
    }

    // ── RegistrationRequest ───────────────────────────────────────────────

    @Test
    void registrationRequest_WithValidValues_ShouldHaveNoViolations() {
        RegistrationRequest dto = new RegistrationRequest();
        dto.setAuthType(AuthType.EMAIL_PASSWORD);
        dto.setName("John Doe");
        dto.setEmail("john@test.com");
        dto.setPassword("Pass@1234");

        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void registrationRequest_WithNullAuthType_ShouldFailValidation() {
        RegistrationRequest dto = new RegistrationRequest();
        dto.setName("John Doe");
        dto.setEmail("john@test.com");
        dto.setPassword("Pass@1234");

        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void registrationRequest_WithBlankName_ShouldFailValidation() {
        RegistrationRequest dto = new RegistrationRequest();
        dto.setAuthType(AuthType.EMAIL_PASSWORD);
        dto.setName("");
        dto.setEmail("john@test.com");
        dto.setPassword("Pass@1234");

        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void registrationRequest_WithInvalidEmail_ShouldFailValidation() {
        RegistrationRequest dto = new RegistrationRequest();
        dto.setAuthType(AuthType.EMAIL_PASSWORD);
        dto.setName("John Doe");
        dto.setEmail("not-an-email");
        dto.setPassword("Pass@1234");

        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void registrationRequest_WithWeakPassword_ShouldFailValidation() {
        RegistrationRequest dto = new RegistrationRequest();
        dto.setAuthType(AuthType.EMAIL_PASSWORD);
        dto.setName("John Doe");
        dto.setEmail("john@test.com");
        dto.setPassword("nouppercase1");

        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    // ── AuthRequest ───────────────────────────────────────────────────────

    @Test
    void authRequest_WithValidValues_ShouldHaveNoViolations() {
        AuthRequest dto = new AuthRequest();
        dto.setAuthType(AuthType.EMAIL_PASSWORD);
        dto.setUsernameOrEmail("john");
        dto.setPassword("Pass@1234");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void authRequest_WithNullAuthType_ShouldFailValidation() {
        AuthRequest dto = new AuthRequest();
        dto.setUsernameOrEmail("john");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    // ── OtpRequestDto ─────────────────────────────────────────────────────

    @Test
    void otpRequestDto_WithValidValues_ShouldHaveNoViolations() {
        OtpRequestDto dto = new OtpRequestDto("+977-9800000000", IdentifierType.PHONE, OtpPurpose.LOGIN);

        Set<ConstraintViolation<OtpRequestDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void otpRequestDto_WithBlankIdentifier_ShouldFailValidation() {
        OtpRequestDto dto = new OtpRequestDto("", IdentifierType.PHONE, OtpPurpose.LOGIN);

        Set<ConstraintViolation<OtpRequestDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void otpRequestDto_WithNullPurpose_ShouldFailValidation() {
        OtpRequestDto dto = new OtpRequestDto("+977-9800000000", IdentifierType.PHONE, null);

        Set<ConstraintViolation<OtpRequestDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    // ── OtpVerifyDto ──────────────────────────────────────────────────────

    @Test
    void otpVerifyDto_WithValidValues_ShouldHaveNoViolations() {
        OtpVerifyDto dto = new OtpVerifyDto("+977-9800000000", "123456", OtpPurpose.LOGIN);

        Set<ConstraintViolation<OtpVerifyDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void otpVerifyDto_WithBlankPhone_ShouldFailValidation() {
        OtpVerifyDto dto = new OtpVerifyDto("", "123456", OtpPurpose.LOGIN);

        Set<ConstraintViolation<OtpVerifyDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void otpVerifyDto_WithBlankOtp_ShouldFailValidation() {
        OtpVerifyDto dto = new OtpVerifyDto("+977-9800000000", "", OtpPurpose.LOGIN);

        Set<ConstraintViolation<OtpVerifyDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    // ── RefreshTokenRequest ───────────────────────────────────────────────

    @Test
    void refreshTokenRequest_WithBlankToken_ShouldFailValidation() {
        RefreshTokenRequest dto = new RefreshTokenRequest();
        dto.setRefreshToken("");

        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    // ── LogoutRequest ─────────────────────────────────────────────────────

    @Test
    void logoutRequest_WithBlankToken_ShouldFailValidation() {
        LogoutRequest dto = new LogoutRequest();
        dto.setRefreshToken("");

        Set<ConstraintViolation<LogoutRequest>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    // ── RegisterDto ───────────────────────────────────────────────────────

    @Test
    void registerDto_WithValidValues_ShouldHaveNoViolations() {
        RegisterDto dto = new RegisterDto("John Doe", "johndoe", "john@test.com", "Pass@1234", "+977-9800000000");

        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void registerDto_WithInvalidPhone_ShouldFailValidation() {
        RegisterDto dto = new RegisterDto("John Doe", "johndoe", "john@test.com", "Pass@1234", "abc");

        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    // ── AuthResult ────────────────────────────────────────────────────────

    @Test
    void authResult_BuilderAndGetters_ShouldWork() {
        AuthResult result = AuthResult.builder().userId(1L).username("john").build();

        assertEquals(1L, result.getUserId());
        assertEquals("john", result.getUsername());
    }

    // ── UserProfileResponse ───────────────────────────────────────────────

    @Test
    void userProfileResponse_BuilderAndGetters_ShouldWork() {
        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L)
                .name("John Doe")
                .username("john")
                .email("john@test.com")
                .phone("+977-9800000000")
                .enabled(true)
                .emailVerified(true)
                .phoneVerified(false)
                .roles(Set.of("ROLE_USER"))
                .build();

        assertEquals(1L, profile.getId());
        assertEquals("John Doe", profile.getName());
        assertEquals("john", profile.getUsername());
        assertEquals("john@test.com", profile.getEmail());
        assertEquals("+977-9800000000", profile.getPhone());
        assertTrue(profile.isEnabled());
        assertTrue(profile.isEmailVerified());
        assertFalse(profile.isPhoneVerified());
        assertTrue(profile.getRoles().contains("ROLE_USER"));
    }
}
