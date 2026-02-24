package com.nifilili.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nifilili.auth.dto.request.LoginDto;
import com.nifilili.auth.dto.request.RegisterDto;
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
    void login_WhenPayloadIsValid_ShouldReturnBearerTokenWithUserProfile() throws Exception {
        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L).username("john").email("john@test.com")
                .enabled(true).roles(Set.of("ROLE_USER")).build();
        JwtAuthResponse stubResponse = JwtAuthResponse.builder()
                .accessToken("token-abc").tokenType("Bearer").user(profile).build();

        when(authService.login(any(LoginDto.class))).thenReturn(stubResponse);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginDto("john", "pwd"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token-abc"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("john"));

        verify(authService, times(1)).login(any(LoginDto.class));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // register
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void register_WhenPayloadIsValid_ShouldReturn201WithTokenAndProfile() throws Exception {
        UserProfileResponse profile = UserProfileResponse.builder()
                .id(10L).username("newuser").email("new@test.com")
                .enabled(true).roles(Set.of("ROLE_USER")).build();
        JwtAuthResponse stubResponse = JwtAuthResponse.builder()
                .accessToken("reg-token").tokenType("Bearer").user(profile).build();

        when(authService.register(any(RegisterDto.class))).thenReturn(stubResponse);

        RegisterDto dto = new RegisterDto("New User", "newuser", "new@test.com", "Pass@1234", null);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("reg-token"))
                .andExpect(jsonPath("$.user.username").value("newuser"));

        verify(authService, times(1)).register(any(RegisterDto.class));
    }

    @Test
    void register_WhenEmailIsMissing_ShouldReturn400() throws Exception {
        RegisterDto dto = new RegisterDto("User", "user1", "", "Pass@1234", null);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void register_WhenPasswordTooWeak_ShouldReturn400() throws Exception {
        RegisterDto dto = new RegisterDto("User", "user2", "user2@test.com", "weak", null);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void register_WhenEmailConflict_ShouldReturn409() throws Exception {
        RegisterDto dto = new RegisterDto("User", "user3", "taken@test.com", "Pass@1234", null);
        when(authService.register(any(RegisterDto.class)))
                .thenThrow(new EmailAlreadyExistsException("Email address is already registered: taken@test.com"));

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email address is already registered: taken@test.com"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getCurrentUser (GET /api/auth/me)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getCurrentUser_WhenAuthenticated_ShouldReturn200WithProfile() throws Exception {
        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L).name("John Doe").username("john").email("john@test.com")
                .phone("+977-9800000000").enabled(true).roles(Set.of("ROLE_USER")).build();

        when(authService.getCurrentUser()).thenReturn(profile);

        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("john@test.com"))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(authService, times(1)).getCurrentUser();
    }
}
