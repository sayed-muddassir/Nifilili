package com.nifilili.auth.service.impl;

import com.nifilili.auth.UserPrincipal;
import com.nifilili.auth.config.JwtTokenProvider;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.dto.request.LoginDto;
import com.nifilili.auth.dto.request.RegisterDto;
import com.nifilili.auth.dto.response.JwtAuthResponse;
import com.nifilili.auth.dto.response.UserProfileResponse;
import com.nifilili.auth.repository.RoleRepository;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.core.exception.EmailAlreadyExistsException;
import com.nifilili.core.exception.UsernameAlreadyExistsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    void login_WhenCredentialsValid_ShouldAuthenticateAndReturnTokenWithProfile() {
        LoginDto dto = new LoginDto("john", "pwd");
        Authentication authentication = mock(Authentication.class);
        Role role = new Role(1L, "ROLE_USER");
        User user = User.builder()
                .name("John Doe").username("john").email("john@test.com")
                .enabled(true).roles(Set.of(role)).build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("john");
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("token-123");
        UserPrincipal principal = UserPrincipal.of(1L, "john", "encoded", true,
                Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        JwtAuthResponse response = authService.login(dto);

        assertEquals("token-123", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getUser());
        assertEquals("john", response.getUser().getUsername());
        assertTrue(response.getUser().getRoles().contains("ROLE_USER"));
        assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("john", captor.getValue().getPrincipal());
        assertEquals("pwd", captor.getValue().getCredentials());
    }

    @Test
    void login_WhenAuthenticationFails_ShouldPropagateException() {
        LoginDto dto = new LoginDto("john", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("auth failed"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(dto));

        assertEquals("auth failed", ex.getMessage());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // register
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void register_WhenPayloadIsValid_ShouldSaveUserAndReturnTokenWithProfile() {
        RegisterDto dto = new RegisterDto("New User", "newuser", "new@test.com", "Pass@1234", null);
        Role userRole = new Role(2L, "ROLE_USER");
        User saved = User.builder()
                .name("New User").username("newuser").email("new@test.com")
                .enabled(true).roles(Set.of(userRole)).build();
        Authentication authentication = mock(Authentication.class);

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("Pass@1234")).thenReturn("encoded-pwd");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("reg-token-456");

        JwtAuthResponse response = authService.register(dto);

        assertEquals("reg-token-456", response.getAccessToken());
        assertNotNull(response.getUser());
        assertEquals("newuser", response.getUser().getUsername());
        assertTrue(response.getUser().getRoles().contains("ROLE_USER"));

        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("Pass@1234");
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldThrowAndNotSave() {
        RegisterDto dto = new RegisterDto("User", "user2", "taken@test.com", "Pass@1234", null);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(dto));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WhenUsernameAlreadyTaken_ShouldThrowAndNotSave() {
        RegisterDto dto = new RegisterDto("User", "takenuser", "fresh@test.com", "Pass@1234", null);
        when(userRepository.existsByEmail("fresh@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("takenuser")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> authService.register(dto));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WhenRoleUserNotSeeded_ShouldThrowIllegalState() {
        RegisterDto dto = new RegisterDto("User", "user3", "user3@test.com", "Pass@1234", null);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> authService.register(dto));

        assertTrue(ex.getMessage().contains("ROLE_USER not seeded"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getCurrentUser
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getCurrentUser_WhenAuthenticated_ShouldReturnUserProfile() {
        Role role = new Role(2L, "ROLE_USER");
        User user = User.builder()
                .name("John Doe").username("john").email("john@test.com")
                .phone("+977-9800000000").enabled(true).roles(Set.of(role)).build();

        UserPrincipal principal = UserPrincipal.of(1L, "john", "encoded", true,
                Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse profile = authService.getCurrentUser();

        assertEquals("john", profile.getUsername());
        assertEquals("john@test.com", profile.getEmail());
        assertEquals("+977-9800000000", profile.getPhone());
        assertTrue(profile.isEnabled());
        assertTrue(profile.getRoles().contains("ROLE_USER"));
    }

    @Test
    void getCurrentUser_WhenUserNotFoundInRepository_ShouldThrow() {
        UserPrincipal principal = UserPrincipal.of(99L, "ghost", "encoded", true,
                Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> authService.getCurrentUser());
    }
}
