package com.nifilili.auth.service.impl;

import com.nifilili.auth.UserPrincipal;
import com.nifilili.auth.config.JwtTokenProvider;
import com.nifilili.auth.domain.RefreshToken;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.dto.request.LoginDto;
import com.nifilili.auth.dto.request.RegisterDto;
import com.nifilili.auth.dto.response.JwtAuthResponse;
import com.nifilili.auth.dto.response.UserProfileResponse;
import com.nifilili.auth.repository.LoginHistoryRepository;
import com.nifilili.auth.repository.RoleRepository;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.auth.service.LoginAttemptService;
import com.nifilili.auth.service.TokenService;
import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.core.exception.AccountLockedException;
import com.nifilili.core.exception.EmailAlreadyExistsException;
import com.nifilili.core.exception.UsernameAlreadyExistsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenService tokenService;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private LoginHistoryRepository loginHistoryRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // login
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void login_WhenCredentialsValid_ShouldAuthenticateAndReturnDualTokens() {
        LoginDto dto = new LoginDto("john", "pwd");
        Authentication authentication = mock(Authentication.class);
        Role role = new Role(1L, "ROLE_USER");
        User user = User.builder()
                .name("John Doe").username("john").email("john@test.com")
                .enabled(true).emailVerified(true).roles(Set.of(role)).build();

        UserPrincipal principal = UserPrincipal.of(1L, "john", "encoded", true, true, false,
                Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(1L).token("refresh-123").deviceName("Chrome")
                .expiresAt(LocalDateTime.now().plusDays(30)).revoked(false).build();

        when(loginAttemptService.isAccountLocked("john")).thenReturn(false);
        when(userRepository.findByUsernameOrEmail("john", "john")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtTokenProvider.generateAccessToken(1L, "john")).thenReturn("access-123");
        when(tokenService.createRefreshToken(1L, "Chrome", "192.168.1.1", "Mozilla/5.0"))
                .thenReturn(refreshToken);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(loginHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JwtAuthResponse response = authService.login(dto, "192.168.1.1", "Mozilla/5.0", "Chrome");

        assertEquals("access-123", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("refresh-123", response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals("john", response.getUser().getUsername());
        assertTrue(response.getUser().getRoles().contains("ROLE_USER"));

        verify(loginAttemptService).recordAttempt(1L, "john", "192.168.1.1", true);
        verify(loginHistoryRepository).save(any());
    }

    @Test
    void login_WhenAccountLocked_ShouldThrowAccountLockedException() {
        LoginDto dto = new LoginDto("john", "pwd");
        when(loginAttemptService.isAccountLocked("john")).thenReturn(true);

        assertThrows(AccountLockedException.class,
                () -> authService.login(dto, "192.168.1.1", "Mozilla/5.0", "Chrome"));

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_WhenCredentialsInvalid_ShouldRecordFailedAttemptAndThrow() {
        LoginDto dto = new LoginDto("john", "wrong");
        User user = User.builder().username("john").build();
        user.setId(1L);

        when(loginAttemptService.isAccountLocked("john")).thenReturn(false);
        when(userRepository.findByUsernameOrEmail("john", "john")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(dto, "192.168.1.1", "Mozilla/5.0", "Chrome"));

        verify(loginAttemptService).recordAttempt(1L, "john", "192.168.1.1", false);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // register
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void register_WhenPayloadIsValid_ShouldSaveUserAndReturnDualTokens() {
        RegisterDto dto = new RegisterDto("New User", "newuser", "new@test.com", "Pass@1234", null);
        Role userRole = new Role(2L, "ROLE_USER");
        User saved = User.builder()
                .name("New User").username("newuser").email("new@test.com")
                .enabled(true).emailVerified(false).roles(Set.of(userRole)).build();
        saved.setId(10L);

        Authentication authentication = mock(Authentication.class);
        UserPrincipal principal = UserPrincipal.of(10L, "newuser", "encoded-pwd", true, false, false,
                Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(10L).token("refresh-reg").deviceName("Chrome")
                .expiresAt(LocalDateTime.now().plusDays(30)).revoked(false).build();

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("Pass@1234")).thenReturn("encoded-pwd");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtTokenProvider.generateAccessToken(10L, "newuser")).thenReturn("access-reg");
        when(tokenService.createRefreshToken(10L, "Chrome", "192.168.1.1", "Mozilla/5.0"))
                .thenReturn(refreshToken);
        when(loginHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JwtAuthResponse response = authService.register(dto, "192.168.1.1", "Mozilla/5.0", "Chrome");

        assertEquals("access-reg", response.getAccessToken());
        assertEquals("refresh-reg", response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals("newuser", response.getUser().getUsername());
        assertFalse(response.getUser().isEmailVerified());

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("Pass@1234");
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldThrowAndNotSave() {
        RegisterDto dto = new RegisterDto("User", "user2", "taken@test.com", "Pass@1234", null);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> authService.register(dto, "192.168.1.1", "Mozilla/5.0", "Chrome"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WhenUsernameAlreadyTaken_ShouldThrowAndNotSave() {
        RegisterDto dto = new RegisterDto("User", "takenuser", "fresh@test.com", "Pass@1234", null);
        when(userRepository.existsByEmail("fresh@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("takenuser")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class,
                () -> authService.register(dto, "192.168.1.1", "Mozilla/5.0", "Chrome"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WhenRoleUserNotSeeded_ShouldThrowIllegalState() {
        RegisterDto dto = new RegisterDto("User", "user3", "user3@test.com", "Pass@1234", null);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> authService.register(dto, "192.168.1.1", "Mozilla/5.0", "Chrome"));

        assertTrue(ex.getMessage().contains("ROLE_USER not seeded"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // refresh
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void refresh_WhenTokenValid_ShouldReturnNewDualTokens() {
        RefreshToken rotated = RefreshToken.builder()
                .userId(1L).token("new-refresh").deviceName("Chrome")
                .expiresAt(LocalDateTime.now().plusDays(30)).revoked(false).build();
        User user = User.builder()
                .name("John").username("john").email("john@test.com")
                .enabled(true).emailVerified(true).roles(Set.of(new Role(2L, "ROLE_USER"))).build();
        user.setId(1L);

        when(tokenService.rotateRefreshToken("old-refresh")).thenReturn(rotated);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(1L, "john")).thenReturn("new-access");

        JwtAuthResponse response = authService.refresh("old-refresh");

        assertEquals("new-access", response.getAccessToken());
        assertEquals("new-refresh", response.getRefreshToken());
        assertEquals("john", response.getUser().getUsername());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // logout
    // ─────────────────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────────────────
    // getCurrentUser
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getCurrentUser_WhenAuthenticated_ShouldReturnUserProfile() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        Role role = new Role(2L, "ROLE_USER");
        User user = User.builder()
                .name("John Doe").username("john").email("john@test.com")
                .phone("+977-9800000000").enabled(true).emailVerified(true)
                .roles(Set.of(role)).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse profile = authService.getCurrentUser();

        assertEquals("john", profile.getUsername());
        assertEquals("john@test.com", profile.getEmail());
        assertEquals("+977-9800000000", profile.getPhone());
        assertTrue(profile.isEnabled());
        assertTrue(profile.isEmailVerified());
        assertTrue(profile.getRoles().contains("ROLE_USER"));
    }

    @Test
    void getCurrentUser_WhenUserNotFoundInRepository_ShouldThrow() {
        SecurityContextTestUtil.setAuthenticatedUser(99L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> authService.getCurrentUser());
    }
}
