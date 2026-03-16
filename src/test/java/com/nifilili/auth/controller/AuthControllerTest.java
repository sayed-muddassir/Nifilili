package com.nifilili.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.nifilili.auth.dto.response.JwtAuthResponse;
import com.nifilili.auth.dto.response.UserProfileResponse;
import com.nifilili.auth.service.AuthService;
import com.nifilili.core.exception.EmailAlreadyExistsException;
import com.nifilili.core.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // login
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void login_WhenValidAuthRequest_ShouldReturn200WithTokens() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setAuthType(AuthType.EMAIL_PASSWORD);
        authRequest.setUsernameOrEmail("john");
        authRequest.setPassword("Pass@1234");

        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L).username("john").email("john@test.com")
                .enabled(true).roles(Set.of("ROLE_USER")).build();
        JwtAuthResponse stubResponse = JwtAuthResponse.builder()
                .accessToken("access-123").tokenType("Bearer")
                .refreshToken("refresh-123").user(profile).build();

        when(authService.login(any(), anyString(), anyString(), anyString())).thenReturn(stubResponse);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_WhenAuthTypeMissing_ShouldReturn400() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsernameOrEmail("john");

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(), anyString(), anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // register
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void register_WhenValidRegistrationRequest_ShouldReturn201() throws Exception {
        RegistrationRequest regRequest = new RegistrationRequest();
        regRequest.setAuthType(AuthType.EMAIL_PASSWORD);
        regRequest.setName("John Doe");
        regRequest.setEmail("john@test.com");
        regRequest.setPassword("Pass@1234");

        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L).username("johndoe").email("john@test.com")
                .enabled(true).roles(Set.of("ROLE_USER")).build();
        JwtAuthResponse stubResponse = JwtAuthResponse.builder()
                .accessToken("access-reg").tokenType("Bearer")
                .refreshToken("refresh-reg").user(profile).build();

        when(authService.register(any(), anyString(), anyString(), anyString())).thenReturn(stubResponse);

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-reg"));
    }
    @Test
    void register_WhenEmailIsMissing_ShouldReturn400() throws Exception {
        RegisterDto dto = new RegisterDto("User", "user1", "", "Pass@1234", null);

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(), any(), any(), any());
    }

    @Test
    void register_WhenPasswordTooWeak_ShouldReturn400() throws Exception {
        RegisterDto dto = new RegisterDto("User", "user2", "user2@test.com", "weak", null);

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(), any(), any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // refresh
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void refresh_WhenTokenValid_ShouldReturn200WithNewTokens() throws Exception {
        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L).username("john").email("john@test.com")
                .enabled(true).roles(Set.of("ROLE_USER")).build();
        JwtAuthResponse stubResponse = JwtAuthResponse.builder()
                .accessToken("new-access").tokenType("Bearer")
                .refreshToken("new-refresh").user(profile).build();

        when(authService.refresh("old-refresh")).thenReturn(stubResponse);

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("old-refresh");

        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // logout
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void logout_WhenCalled_ShouldReturn204() throws Exception {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("some-token");

        mvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).logout("some-token");
    }

    @Test
    void logoutAll_WhenCalled_ShouldReturn204() throws Exception {
        mvc.perform(post("/api/v1/auth/logout-all"))
                .andExpect(status().isNoContent());

        verify(authService).logoutAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getCurrentUser (GET /api/auth/me)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getCurrentUser_WhenAuthenticated_ShouldReturn200WithProfile() throws Exception {
        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L).name("John Doe").username("john").email("john@test.com")
                .phone("+977-9800000000").enabled(true).emailVerified(true)
                .roles(Set.of("ROLE_USER")).build();

        when(authService.getCurrentUser()).thenReturn(profile);

        mvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("john@test.com"))
                .andExpect(jsonPath("$.emailVerified").value(true))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(authService).getCurrentUser();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // requestOtp
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void requestOtp_WhenValidRequest_ShouldReturn200WithMessage() throws Exception {
        OtpRequestDto otpRequest = new OtpRequestDto();
        otpRequest.setIdentifier("+977-9800000000");
        otpRequest.setIdentifierType(IdentifierType.PHONE);
        otpRequest.setPurpose(OtpPurpose.LOGIN);

        mvc.perform(post("/api/v1/auth/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent successfully"));

        verify(authService).requestOtp(any());
    }

    @Test
    void requestOtp_WhenIdentifierBlank_ShouldReturn400() throws Exception {
        OtpRequestDto otpRequest = new OtpRequestDto();
        otpRequest.setIdentifier("");
        otpRequest.setIdentifierType(IdentifierType.PHONE);
        otpRequest.setPurpose(OtpPurpose.LOGIN);

        mvc.perform(post("/api/v1/auth/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).requestOtp(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // verifyOtp
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void verifyOtp_WhenValidOtp_ShouldReturn200WithTokens() throws Exception {
        OtpVerifyDto verifyDto = new OtpVerifyDto();
        verifyDto.setPhone("+977-9800000000");
        verifyDto.setOtp("123456");
        verifyDto.setPurpose(OtpPurpose.LOGIN);

        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L).username("phoneuser")
                .enabled(true).roles(Set.of("ROLE_USER")).build();
        JwtAuthResponse stubResponse = JwtAuthResponse.builder()
                .accessToken("access-otp").tokenType("Bearer")
                .refreshToken("refresh-otp").user(profile).build();

        when(authService.verifyOtpAndLogin(any(), anyString(), anyString(), anyString())).thenReturn(stubResponse);

        mvc.perform(post("/api/v1/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-otp"));
    }

    @Test
    void verifyOtp_WhenPhoneBlank_ShouldReturn400() throws Exception {
        OtpVerifyDto verifyDto = new OtpVerifyDto();
        verifyDto.setPhone("");
        verifyDto.setOtp("123456");
        verifyDto.setPurpose(OtpPurpose.LOGIN);

        mvc.perform(post("/api/v1/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).verifyOtpAndLogin(any(), anyString(), anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Request helper methods
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void login_WhenXForwardedForPresent_ShouldExtractClientIp() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setAuthType(AuthType.EMAIL_PASSWORD);
        authRequest.setUsernameOrEmail("john");
        authRequest.setPassword("Pass@1234");

        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L).username("john").enabled(true).roles(Set.of("ROLE_USER")).build();
        JwtAuthResponse stubResponse = JwtAuthResponse.builder()
                .accessToken("a").tokenType("Bearer").refreshToken("r").user(profile).build();

        when(authService.login(any(), eq("10.0.0.1"), anyString(), anyString())).thenReturn(stubResponse);

        mvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", "10.0.0.1, 192.168.1.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk());

        verify(authService).login(any(), eq("10.0.0.1"), anyString(), anyString());
    }

    @Test
    void login_WhenXDeviceNamePresent_ShouldUseDeviceName() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setAuthType(AuthType.EMAIL_PASSWORD);
        authRequest.setUsernameOrEmail("john");
        authRequest.setPassword("Pass@1234");

        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L).username("john").enabled(true).roles(Set.of("ROLE_USER")).build();
        JwtAuthResponse stubResponse = JwtAuthResponse.builder()
                .accessToken("a").tokenType("Bearer").refreshToken("r").user(profile).build();

        when(authService.login(any(), anyString(), anyString(), eq("My Phone"))).thenReturn(stubResponse);

        mvc.perform(post("/api/v1/auth/login")
                        .header("X-Device-Name", "My Phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk());

        verify(authService).login(any(), anyString(), anyString(), eq("My Phone"));
    }

    @Test
    void login_WhenNoDeviceNameAndLongUserAgent_ShouldTruncateAt50() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setAuthType(AuthType.EMAIL_PASSWORD);
        authRequest.setUsernameOrEmail("john");
        authRequest.setPassword("Pass@1234");

        String longUA = "A".repeat(60);
        String expectedDevice = longUA.substring(0, 50);

        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L).username("john").enabled(true).roles(Set.of("ROLE_USER")).build();
        JwtAuthResponse stubResponse = JwtAuthResponse.builder()
                .accessToken("a").tokenType("Bearer").refreshToken("r").user(profile).build();

        when(authService.login(any(), anyString(), eq(longUA), eq(expectedDevice))).thenReturn(stubResponse);

        mvc.perform(post("/api/v1/auth/login")
                        .header("User-Agent", longUA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk());

        verify(authService).login(any(), anyString(), eq(longUA), eq(expectedDevice));
    }
}
