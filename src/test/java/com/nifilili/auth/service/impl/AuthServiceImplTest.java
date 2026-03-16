package com.nifilili.auth.service.impl;

import com.nifilili.auth.config.JwtTokenProvider;
import com.nifilili.auth.domain.RefreshToken;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.domain.enums.AuthType;
import com.nifilili.auth.domain.enums.IdentifierType;
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
import com.nifilili.auth.service.OtpService;
import com.nifilili.auth.service.TokenService;
import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.core.exception.InvalidTokenException;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserRepository userRepository;
    @Mock private TokenService tokenService;
    @Mock private AuthProviderRouter authProviderRouter;
    @Mock private OtpService otpService;

    @InjectMocks private AuthServiceImpl authService;

    @AfterEach
    void clearContext() {
        SecurityContextTestUtil.clearContext();
    }

    // ── Unified login (strategy-based) ───────────────────────────────────

    @Test
    void login_WhenAuthRequestValid_ShouldDelegateToRouterAndReturnTokens() {
        AuthRequest request = new AuthRequest();
        request.setAuthType(AuthType.EMAIL_PASSWORD);
        request.setUsernameOrEmail("john");
        request.setPassword("pwd");

        AuthResult authResult = new AuthResult(1L, "john");
        User user = buildTestUser(1L, "John Doe", "john", "john@test.com");
        RefreshToken refreshToken = buildRefreshToken(1L, "refresh-123");

        when(authProviderRouter.authenticate(request, "192.168.1.1", "Mozilla/5.0", "Chrome"))
                .thenReturn(authResult);
        when(jwtTokenProvider.generateAccessToken(1L, "john")).thenReturn("access-123");
        when(tokenService.createRefreshToken(1L, "Chrome", "192.168.1.1", "Mozilla/5.0"))
                .thenReturn(refreshToken);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        JwtAuthResponse response = authService.login(request, "192.168.1.1", "Mozilla/5.0", "Chrome");

        assertEquals("access-123", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("refresh-123", response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals("john", response.getUser().getUsername());
        assertTrue(response.getUser().getRoles().contains("ROLE_USER"));
    }

    // ── Unified registration ─────────────────────────────────────────────

    @Test
    void register_WhenRegistrationRequestValid_ShouldDelegateToRouterAndReturnTokens() {
        RegistrationRequest request = new RegistrationRequest();
        request.setAuthType(AuthType.EMAIL_PASSWORD);
        request.setName("New User");
        request.setEmail("new@test.com");
        request.setPassword("Pass@1234");

        AuthResult authResult = new AuthResult(10L, "newuser");
        User user = buildTestUser(10L, "New User", "newuser", "new@test.com");
        RefreshToken refreshToken = buildRefreshToken(10L, "refresh-reg");

        when(authProviderRouter.register(request, "192.168.1.1", "Mozilla/5.0", "Chrome"))
                .thenReturn(authResult);
        when(jwtTokenProvider.generateAccessToken(10L, "newuser")).thenReturn("access-reg");
        when(tokenService.createRefreshToken(10L, "Chrome", "192.168.1.1", "Mozilla/5.0"))
                .thenReturn(refreshToken);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        JwtAuthResponse response = authService.register(request, "192.168.1.1", "Mozilla/5.0", "Chrome");

        assertEquals("access-reg", response.getAccessToken());
        assertEquals("refresh-reg", response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals("newuser", response.getUser().getUsername());
    }

    // ── Legacy register ──────────────────────────────────────────────────

    // ── OTP request ──────────────────────────────────────────────────────

    @Test
    void requestOtp_WhenSignupAndIdentifierNotRegistered_ShouldDelegateToOtpService() {
        OtpRequestDto dto = new OtpRequestDto();
        dto.setIdentifier("+977-9800000000");
        dto.setIdentifierType(IdentifierType.PHONE);
        dto.setPurpose(OtpPurpose.SIGNUP);

        when(userRepository.existsByPhone("+977-9800000000")).thenReturn(false);

        authService.requestOtp(dto);

        verify(otpService).generateAndSendOtp("+977-9800000000", IdentifierType.PHONE, OtpPurpose.SIGNUP);
    }

    @Test
    void requestOtp_WhenSignupAndPhoneAlreadyRegistered_ShouldThrowInvalidTokenException() {
        OtpRequestDto dto = new OtpRequestDto();
        dto.setIdentifier("+977-9800000000");
        dto.setIdentifierType(IdentifierType.PHONE);
        dto.setPurpose(OtpPurpose.SIGNUP);

        when(userRepository.existsByPhone("+977-9800000000")).thenReturn(true);

        assertThrows(InvalidTokenException.class, () -> authService.requestOtp(dto));

        verify(otpService, never()).generateAndSendOtp(any(), any(), any());
    }

    @Test
    void requestOtp_WhenLoginAndPhoneNotRegistered_ShouldThrowResourceNotFoundException() {
        OtpRequestDto dto = new OtpRequestDto();
        dto.setIdentifier("+977-9800000000");
        dto.setIdentifierType(IdentifierType.PHONE);
        dto.setPurpose(OtpPurpose.LOGIN);

        when(userRepository.existsByPhone("+977-9800000000")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> authService.requestOtp(dto));
    }

    @Test
    void requestOtp_WhenSignupEmailAlreadyRegistered_ShouldThrowInvalidTokenException() {
        OtpRequestDto dto = new OtpRequestDto();
        dto.setIdentifier("existing@test.com");
        dto.setIdentifierType(IdentifierType.EMAIL);
        dto.setPurpose(OtpPurpose.SIGNUP);

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(InvalidTokenException.class, () -> authService.requestOtp(dto));
        verify(otpService, never()).generateAndSendOtp(any(), any(), any());
    }

    @Test
    void requestOtp_WhenSignupEmailNotRegistered_ShouldDelegate() {
        OtpRequestDto dto = new OtpRequestDto();
        dto.setIdentifier("new@test.com");
        dto.setIdentifierType(IdentifierType.EMAIL);
        dto.setPurpose(OtpPurpose.SIGNUP);

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

        authService.requestOtp(dto);

        verify(otpService).generateAndSendOtp("new@test.com", IdentifierType.EMAIL, OtpPurpose.SIGNUP);
    }

    @Test
    void requestOtp_WhenLoginEmailNotRegistered_ShouldSilentlyIgnoreAndStillSendOtp() {
        OtpRequestDto dto = new OtpRequestDto();
        dto.setIdentifier("unknown@test.com");
        dto.setIdentifierType(IdentifierType.EMAIL);
        dto.setPurpose(OtpPurpose.LOGIN);

        when(userRepository.existsByEmail("unknown@test.com")).thenReturn(false);

        // Anti-enumeration: should NOT throw, still sends OTP
        authService.requestOtp(dto);

        verify(otpService).generateAndSendOtp("unknown@test.com", IdentifierType.EMAIL, OtpPurpose.LOGIN);
    }

    @Test
    void requestOtp_WhenLoginEmailRegistered_ShouldDelegate() {
        OtpRequestDto dto = new OtpRequestDto();
        dto.setIdentifier("existing@test.com");
        dto.setIdentifierType(IdentifierType.EMAIL);
        dto.setPurpose(OtpPurpose.LOGIN);

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        authService.requestOtp(dto);

        verify(otpService).generateAndSendOtp("existing@test.com", IdentifierType.EMAIL, OtpPurpose.LOGIN);
    }

    // ── OTP verify and login ─────────────────────────────────────────────

    @Test
    void verifyOtpAndLogin_WhenValidOtp_ShouldAuthenticateViaRouter() {
        OtpVerifyDto dto = new OtpVerifyDto();
        dto.setPhone("+977-9800000000");
        dto.setOtp("123456");

        AuthResult authResult = new AuthResult(1L, "phoneuser");
        User user = buildTestUser(1L, "Phone User", "phoneuser", null);
        RefreshToken refreshToken = buildRefreshToken(1L, "refresh-otp");

        when(authProviderRouter.authenticate(any(AuthRequest.class), anyString(), anyString(), anyString()))
                .thenReturn(authResult);
        when(jwtTokenProvider.generateAccessToken(1L, "phoneuser")).thenReturn("access-otp");
        when(tokenService.createRefreshToken(1L, "Chrome", "192.168.1.1", "Mozilla/5.0"))
                .thenReturn(refreshToken);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        JwtAuthResponse response = authService.verifyOtpAndLogin(dto, "192.168.1.1", "Mozilla/5.0", "Chrome");

        assertEquals("access-otp", response.getAccessToken());
        verify(authProviderRouter).authenticate(argThat(req ->
                req.getAuthType() == AuthType.PHONE_OTP
                        && "+977-9800000000".equals(req.getPhone())
                        && "123456".equals(req.getOtp())
        ), anyString(), anyString(), anyString());
    }

    // ── Token refresh ────────────────────────────────────────────────────

    @Test
    void refresh_WhenTokenValid_ShouldReturnNewDualTokens() {
        RefreshToken rotated = buildRefreshToken(1L, "new-refresh");
        User user = buildTestUser(1L, "John", "john", "john@test.com");

        when(tokenService.rotateRefreshToken("old-refresh")).thenReturn(rotated);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(1L, "john")).thenReturn("new-access");

        JwtAuthResponse response = authService.refresh("old-refresh");

        assertEquals("new-access", response.getAccessToken());
        assertEquals("new-refresh", response.getRefreshToken());
        assertEquals("john", response.getUser().getUsername());
    }

    // ── Logout ───────────────────────────────────────────────────────────

    @Test
    void logout_WhenCalled_ShouldRevokeSpecificToken() {
        authService.logout("some-refresh-token");

        verify(tokenService).revokeRefreshToken("some-refresh-token");
    }

    @Test
    void logoutAll_WhenCalled_ShouldRevokeAllTokensForCurrentUser() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);

        authService.logoutAll();

        verify(tokenService).revokeAllRefreshTokens(1L);
    }

    // ── getCurrentUser ───────────────────────────────────────────────────

    @Test
    void getCurrentUser_WhenAuthenticated_ShouldReturnUserProfile() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        User user = buildTestUser(1L, "John Doe", "john", "john@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse profile = authService.getCurrentUser();

        assertEquals("john", profile.getUsername());
        assertEquals("john@test.com", profile.getEmail());
        assertTrue(profile.isEnabled());
        assertTrue(profile.getRoles().contains("ROLE_USER"));
    }

    @Test
    void getCurrentUser_WhenUserNotFoundInRepository_ShouldThrow() {
        SecurityContextTestUtil.setAuthenticatedUser(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> authService.getCurrentUser());
    }

    // ── Test helpers ─────────────────────────────────────────────────────

    private User buildTestUser(Long id, String name, String username, String email) {
        Role role = new Role(1L, "ROLE_USER");
        User user = User.builder()
                .name(name).username(username).email(email)
                .enabled(true).emailVerified(true).phoneVerified(false)
                .roles(Set.of(role)).build();
        user.setId(id);
        return user;
    }

    private RefreshToken buildRefreshToken(Long userId, String token) {
        return RefreshToken.builder()
                .userId(userId).token(token).deviceName("Chrome")
                .expiresAt(LocalDateTime.now().plusDays(30)).revoked(false).build();
    }
}
