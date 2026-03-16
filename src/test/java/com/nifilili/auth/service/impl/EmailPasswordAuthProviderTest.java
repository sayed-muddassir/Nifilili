package com.nifilili.auth.service.impl;

import com.nifilili.auth.UserPrincipal;
import com.nifilili.auth.domain.LoginHistory;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.domain.UserAuthProvider;
import com.nifilili.auth.domain.enums.AuthType;
import com.nifilili.auth.dto.request.AuthRequest;
import com.nifilili.auth.dto.request.RegistrationRequest;
import com.nifilili.auth.dto.response.AuthResult;
import com.nifilili.auth.events.LoginAttemptEvent;
import com.nifilili.auth.repository.LoginHistoryRepository;
import com.nifilili.auth.repository.RoleRepository;
import com.nifilili.auth.repository.UserAuthProviderRepository;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.auth.service.LoginAttemptService;
import com.nifilili.account.events.UserRegisteredEvent;
import com.nifilili.core.exception.AccountLockedException;
import com.nifilili.core.exception.EmailAlreadyExistsException;
import com.nifilili.core.exception.UsernameAlreadyExistsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailPasswordAuthProviderTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private LoginHistoryRepository loginHistoryRepository;
    @Mock private UserAuthProviderRepository authProviderRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private EmailPasswordAuthProvider provider;

    @Captor private ArgumentCaptor<LoginAttemptEvent> attemptEventCaptor;
    @Captor private ArgumentCaptor<User> userCaptor;
    @Captor private ArgumentCaptor<LoginHistory> historyCaptor;
    @Captor private ArgumentCaptor<UserAuthProvider> authProviderCaptor;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── supports ──────────────────────────────────────────────────────────

    @Test
    void supports_WhenEmailPassword_ShouldReturnTrue() {
        assertTrue(provider.supports(AuthType.EMAIL_PASSWORD));
    }

    @Test
    void supports_WhenPhoneOtp_ShouldReturnFalse() {
        assertFalse(provider.supports(AuthType.PHONE_OTP));
    }

    // ── authenticate ──────────────────────────────────────────────────────

    @Test
    void authenticate_WhenValidCredentials_ShouldReturnAuthResultAndRecordHistory() {
        AuthRequest request = new AuthRequest();
        request.setAuthType(AuthType.EMAIL_PASSWORD);
        request.setUsernameOrEmail("john");
        request.setPassword("Pass@1234");

        User user = buildTestUser(1L, "John Doe", "john", "john@test.com");
        UserPrincipal principal = UserPrincipal.of(1L, "john", "encoded", true, true, false,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authentication = mock(Authentication.class);

        when(loginAttemptService.isAccountLocked("john")).thenReturn(false);
        when(userRepository.findByUsernameOrEmail("john", "john")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);

        AuthResult result = provider.authenticate(request, "192.168.1.1", "Mozilla/5.0", "Chrome");

        assertEquals(1L, result.getUserId());
        assertEquals("john", result.getUsername());

        // Verify success event published
        verify(eventPublisher).publishEvent(attemptEventCaptor.capture());
        LoginAttemptEvent event = attemptEventCaptor.getValue();
        assertEquals(1L, event.userId());
        assertTrue(event.success());
        assertEquals("192.168.1.1", event.ipAddress());

        // Verify login history recorded
        verify(loginHistoryRepository).save(historyCaptor.capture());
        LoginHistory history = historyCaptor.getValue();
        assertEquals(1L, history.getUserId());
        assertEquals("192.168.1.1", history.getIpAddress());
    }

    @Test
    void authenticate_WhenAccountLocked_ShouldThrowAccountLockedException() {
        AuthRequest request = new AuthRequest();
        request.setUsernameOrEmail("lockeduser");

        when(loginAttemptService.isAccountLocked("lockeduser")).thenReturn(true);

        assertThrows(AccountLockedException.class,
                () -> provider.authenticate(request, "192.168.1.1", "Mozilla/5.0", "Chrome"));

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void authenticate_WhenBadCredentials_ShouldPublishFailedAttemptAndRethrow() {
        AuthRequest request = new AuthRequest();
        request.setUsernameOrEmail("john");
        request.setPassword("wrongpass");

        User user = buildTestUser(1L, "John Doe", "john", "john@test.com");

        when(loginAttemptService.isAccountLocked("john")).thenReturn(false);
        when(userRepository.findByUsernameOrEmail("john", "john")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> provider.authenticate(request, "192.168.1.1", "Mozilla/5.0", "Chrome"));

        verify(eventPublisher).publishEvent(attemptEventCaptor.capture());
        LoginAttemptEvent event = attemptEventCaptor.getValue();
        assertEquals(1L, event.userId());
        assertFalse(event.success());
    }

    @Test
    void authenticate_WhenUserNotFoundInRepo_ShouldPublishAttemptWithNullUserId() {
        AuthRequest request = new AuthRequest();
        request.setUsernameOrEmail("unknown");
        request.setPassword("pass");

        when(loginAttemptService.isAccountLocked("unknown")).thenReturn(false);
        when(userRepository.findByUsernameOrEmail("unknown", "unknown")).thenReturn(Optional.empty());
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> provider.authenticate(request, "192.168.1.1", "Mozilla/5.0", "Chrome"));

        verify(eventPublisher).publishEvent(attemptEventCaptor.capture());
        assertNull(attemptEventCaptor.getValue().userId());
        assertFalse(attemptEventCaptor.getValue().success());
    }

    // ── register ──────────────────────────────────────────────────────────

    @Test
    void register_WhenValidRequest_ShouldCreateUserAndPublishEvent() {
        RegistrationRequest request = buildRegistrationRequest("John Doe", "johndoe", "john@test.com", "Pass@1234");
        Role userRole = new Role(1L, "ROLE_USER");

        User savedUser = buildTestUser(10L, "John Doe", "johndoe", "john@test.com");
        Authentication authentication = mock(Authentication.class);

        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("Pass@1234")).thenReturn("encoded-pwd");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        AuthResult result = provider.register(request, "192.168.1.1", "Mozilla/5.0", "Chrome");

        assertEquals(10L, result.getUserId());
        assertEquals("johndoe", result.getUsername());

        // Verify user saved with encoded password
        verify(userRepository).save(userCaptor.capture());
        assertEquals("encoded-pwd", userCaptor.getValue().getPassword());
        assertTrue(userCaptor.getValue().isEnabled());
        assertFalse(userCaptor.getValue().isEmailVerified());

        // Verify auth provider tracked
        verify(authProviderRepository).save(any(UserAuthProvider.class));

        // Verify event published
        verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));

        // Verify login history
        verify(loginHistoryRepository).save(any(LoginHistory.class));
    }

    @Test
    void register_WhenEmailNull_ShouldThrowIllegalArgumentException() {
        RegistrationRequest request = buildRegistrationRequest("John", "johndoe", null, "Pass@1234");

        assertThrows(IllegalArgumentException.class,
                () -> provider.register(request, "ip", "ua", "device"));
    }

    @Test
    void register_WhenPasswordNull_ShouldThrowIllegalArgumentException() {
        RegistrationRequest request = buildRegistrationRequest("John", "johndoe", "john@test.com", null);

        assertThrows(IllegalArgumentException.class,
                () -> provider.register(request, "ip", "ua", "device"));
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldThrowEmailAlreadyExistsException() {
        RegistrationRequest request = buildRegistrationRequest("John", "johndoe", "taken@test.com", "Pass@1234");

        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> provider.register(request, "ip", "ua", "device"));
    }

    @Test
    void register_WhenUsernameAlreadyExists_ShouldThrowUsernameAlreadyExistsException() {
        RegistrationRequest request = buildRegistrationRequest("John", "taken", "john@test.com", "Pass@1234");

        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class,
                () -> provider.register(request, "ip", "ua", "device"));
    }

    @Test
    void register_WhenRoleNotSeeded_ShouldThrowIllegalStateException() {
        RegistrationRequest request = buildRegistrationRequest("John", "johndoe", "john@test.com", "Pass@1234");

        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> provider.register(request, "ip", "ua", "device"));
    }

    // ── resolveUsername ───────────────────────────────────────────────────

    @Test
    void register_WhenUsernameProvided_ShouldUseProvidedUsername() {
        RegistrationRequest request = buildRegistrationRequest("John", "customuser", "john@test.com", "Pass@1234");
        Role userRole = new Role(1L, "ROLE_USER");
        User savedUser = buildTestUser(10L, "John", "customuser", "john@test.com");
        Authentication auth = mock(Authentication.class);

        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("customuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        provider.register(request, "ip", "ua", "device");

        verify(userRepository).save(userCaptor.capture());
        assertEquals("customuser", userCaptor.getValue().getUsername());
    }

    @Test
    void register_WhenUsernameNotProvided_ShouldAutoGenerateFromName() {
        RegistrationRequest request = buildRegistrationRequest("John Doe", null, "john@test.com", "Pass@1234");
        Role userRole = new Role(1L, "ROLE_USER");
        User savedUser = buildTestUser(10L, "John Doe", "johndoe", "john@test.com");
        Authentication auth = mock(Authentication.class);

        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        provider.register(request, "ip", "ua", "device");

        verify(userRepository).save(userCaptor.capture());
        assertEquals("johndoe", userCaptor.getValue().getUsername());
    }

    @Test
    void register_WhenAutoGeneratedUsernameExists_ShouldAppendSuffix() {
        RegistrationRequest request = buildRegistrationRequest("John Doe", null, "john@test.com", "Pass@1234");
        Role userRole = new Role(1L, "ROLE_USER");
        User savedUser = buildTestUser(10L, "John Doe", "johndoe1", "john@test.com");
        Authentication auth = mock(Authentication.class);

        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        // First check for "johndoe" -> taken, then "johndoe1" -> available
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);
        when(userRepository.existsByUsername("johndoe1")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        provider.register(request, "ip", "ua", "device");

        verify(userRepository).save(userCaptor.capture());
        assertEquals("johndoe1", userCaptor.getValue().getUsername());
    }

    // ── Test helpers ─────────────────────────────────────────────────────

    private User buildTestUser(Long id, String name, String username, String email) {
        Role role = new Role(1L, "ROLE_USER");
        User user = User.builder()
                .name(name).username(username).email(email)
                .enabled(true).emailVerified(true).phoneVerified(false)
                .accountLocked(false).roles(Set.of(role)).build();
        user.setId(id);
        return user;
    }

    private RegistrationRequest buildRegistrationRequest(String name, String username, String email, String password) {
        RegistrationRequest request = new RegistrationRequest();
        request.setAuthType(AuthType.EMAIL_PASSWORD);
        request.setName(name);
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
