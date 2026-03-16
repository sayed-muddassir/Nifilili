package com.nifilili.auth.service.impl;

import com.nifilili.auth.domain.LoginHistory;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.domain.UserAuthProvider;
import com.nifilili.auth.domain.enums.AuthType;
import com.nifilili.auth.domain.enums.OtpPurpose;
import com.nifilili.auth.dto.request.AuthRequest;
import com.nifilili.auth.dto.request.RegistrationRequest;
import com.nifilili.auth.dto.response.AuthResult;
import com.nifilili.auth.repository.LoginHistoryRepository;
import com.nifilili.auth.repository.RoleRepository;
import com.nifilili.auth.repository.UserAuthProviderRepository;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.auth.service.OtpService;
import com.nifilili.account.events.UserRegisteredEvent;
import com.nifilili.core.exception.InvalidTokenException;
import com.nifilili.core.exception.PhoneAlreadyExistsException;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhoneOtpAuthProviderTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private OtpService otpService;
    @Mock private LoginHistoryRepository loginHistoryRepository;
    @Mock private UserAuthProviderRepository authProviderRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private PhoneOtpAuthProvider provider;

    @Captor private ArgumentCaptor<User> userCaptor;
    @Captor private ArgumentCaptor<LoginHistory> historyCaptor;

    // ── supports ──────────────────────────────────────────────────────────

    @Test
    void supports_WhenPhoneOtp_ShouldReturnTrue() {
        assertTrue(provider.supports(AuthType.PHONE_OTP));
    }

    @Test
    void supports_WhenEmailPassword_ShouldReturnFalse() {
        assertFalse(provider.supports(AuthType.EMAIL_PASSWORD));
    }

    // ── authenticate ──────────────────────────────────────────────────────

    @Test
    void authenticate_WhenValidOtpAndUserExists_ShouldReturnAuthResult() {
        AuthRequest request = new AuthRequest();
        request.setAuthType(AuthType.PHONE_OTP);
        request.setPhone("+977-9800000000");
        request.setOtp("123456");

        User user = buildTestUser(1L, "Phone User", "phoneuser", null, "+977-9800000000");

        when(otpService.verifyOtp("+977-9800000000", "123456", OtpPurpose.LOGIN)).thenReturn(true);
        when(userRepository.findByPhone("+977-9800000000")).thenReturn(Optional.of(user));

        AuthResult result = provider.authenticate(request, "192.168.1.1", "Mozilla/5.0", "Chrome");

        assertEquals(1L, result.getUserId());
        assertEquals("phoneuser", result.getUsername());

        verify(loginHistoryRepository).save(historyCaptor.capture());
        assertEquals(1L, historyCaptor.getValue().getUserId());
    }

    @Test
    void authenticate_WhenInvalidOtp_ShouldThrowInvalidTokenException() {
        AuthRequest request = new AuthRequest();
        request.setPhone("+977-9800000000");
        request.setOtp("000000");

        when(otpService.verifyOtp("+977-9800000000", "000000", OtpPurpose.LOGIN)).thenReturn(false);

        assertThrows(InvalidTokenException.class,
                () -> provider.authenticate(request, "ip", "ua", "device"));
    }

    @Test
    void authenticate_WhenUserNotFoundByPhone_ShouldThrowResourceNotFoundException() {
        AuthRequest request = new AuthRequest();
        request.setPhone("+977-9800000000");
        request.setOtp("123456");

        when(otpService.verifyOtp("+977-9800000000", "123456", OtpPurpose.LOGIN)).thenReturn(true);
        when(userRepository.findByPhone("+977-9800000000")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> provider.authenticate(request, "ip", "ua", "device"));
    }

    // ── register ──────────────────────────────────────────────────────────

    @Test
    void register_WhenValidRequest_ShouldCreateUserAndPublishEvent() {
        RegistrationRequest request = buildRegRequest("John Doe", "johndoe", "john@test.com",
                "Pass@1234", "+977-9800000000", "123456");
        Role userRole = new Role(1L, "ROLE_USER");
        User savedUser = buildTestUser(10L, "John Doe", "johndoe", "john@test.com", "+977-9800000000");

        when(otpService.verifyOtp("+977-9800000000", "123456", OtpPurpose.SIGNUP)).thenReturn(true);
        when(userRepository.existsByPhone("+977-9800000000")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("Pass@1234")).thenReturn("encoded-pwd");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResult result = provider.register(request, "192.168.1.1", "Mozilla/5.0", "Chrome");

        assertEquals(10L, result.getUserId());
        assertEquals("johndoe", result.getUsername());

        // Verify user saved with phoneVerified=true
        verify(userRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().isPhoneVerified());
        assertEquals("encoded-pwd", userCaptor.getValue().getPassword());

        verify(authProviderRepository).save(any(UserAuthProvider.class));
        verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
        verify(loginHistoryRepository).save(any(LoginHistory.class));
    }

    @Test
    void register_WhenPhoneNull_ShouldThrowIllegalArgumentException() {
        RegistrationRequest request = buildRegRequest("John", "johndoe", null, "Pass@1234", null, "123456");

        assertThrows(IllegalArgumentException.class,
                () -> provider.register(request, "ip", "ua", "device"));
    }

    @Test
    void register_WhenPasswordNull_ShouldThrowIllegalArgumentException() {
        RegistrationRequest request = buildRegRequest("John", "johndoe", null, null, "+977-9800000000", "123456");

        assertThrows(IllegalArgumentException.class,
                () -> provider.register(request, "ip", "ua", "device"));
    }

    @Test
    void register_WhenOtpInvalid_ShouldThrowInvalidTokenException() {
        RegistrationRequest request = buildRegRequest("John", "johndoe", null, "Pass@1234", "+977-9800000000", "000000");

        when(otpService.verifyOtp("+977-9800000000", "000000", OtpPurpose.SIGNUP)).thenReturn(false);

        assertThrows(InvalidTokenException.class,
                () -> provider.register(request, "ip", "ua", "device"));
    }

    @Test
    void register_WhenPhoneAlreadyExists_ShouldThrowPhoneAlreadyExistsException() {
        RegistrationRequest request = buildRegRequest("John", "johndoe", null, "Pass@1234", "+977-9800000000", "123456");

        when(otpService.verifyOtp("+977-9800000000", "123456", OtpPurpose.SIGNUP)).thenReturn(true);
        when(userRepository.existsByPhone("+977-9800000000")).thenReturn(true);

        assertThrows(PhoneAlreadyExistsException.class,
                () -> provider.register(request, "ip", "ua", "device"));
    }

    @Test
    void register_WhenEmailProvidedAndAlreadyExists_ShouldThrowException() {
        RegistrationRequest request = buildRegRequest("John", "johndoe", "taken@test.com",
                "Pass@1234", "+977-9800000000", "123456");

        when(otpService.verifyOtp("+977-9800000000", "123456", OtpPurpose.SIGNUP)).thenReturn(true);
        when(userRepository.existsByPhone("+977-9800000000")).thenReturn(false);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThrows(PhoneAlreadyExistsException.class,
                () -> provider.register(request, "ip", "ua", "device"));
    }

    @Test
    void register_WhenEmailNullOrBlank_ShouldSkipEmailCheck() {
        RegistrationRequest request = buildRegRequest("John Doe", "johndoe", null,
                "Pass@1234", "+977-9800000000", "123456");
        Role userRole = new Role(1L, "ROLE_USER");
        User savedUser = buildTestUser(10L, "John Doe", "johndoe", null, "+977-9800000000");

        when(otpService.verifyOtp("+977-9800000000", "123456", OtpPurpose.SIGNUP)).thenReturn(true);
        when(userRepository.existsByPhone("+977-9800000000")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        provider.register(request, "ip", "ua", "device");

        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void register_WhenRoleNotSeeded_ShouldThrowIllegalStateException() {
        RegistrationRequest request = buildRegRequest("John", "johndoe", null,
                "Pass@1234", "+977-9800000000", "123456");

        when(otpService.verifyOtp("+977-9800000000", "123456", OtpPurpose.SIGNUP)).thenReturn(true);
        when(userRepository.existsByPhone("+977-9800000000")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> provider.register(request, "ip", "ua", "device"));
    }

    // ── resolveUsername ───────────────────────────────────────────────────

    @Test
    void register_WhenAutoGeneratedUsernameCollides_ShouldAppendSuffix() {
        RegistrationRequest request = buildRegRequest("John Doe", null, null,
                "Pass@1234", "+977-9800000000", "123456");
        Role userRole = new Role(1L, "ROLE_USER");
        User savedUser = buildTestUser(10L, "John Doe", "johndoe1", null, "+977-9800000000");

        when(otpService.verifyOtp("+977-9800000000", "123456", OtpPurpose.SIGNUP)).thenReturn(true);
        when(userRepository.existsByPhone("+977-9800000000")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);
        when(userRepository.existsByUsername("johndoe1")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        provider.register(request, "ip", "ua", "device");

        verify(userRepository).save(userCaptor.capture());
        assertEquals("johndoe1", userCaptor.getValue().getUsername());
    }

    // ── Test helpers ─────────────────────────────────────────────────────

    private User buildTestUser(Long id, String name, String username, String email, String phone) {
        Role role = new Role(1L, "ROLE_USER");
        User user = User.builder()
                .name(name).username(username).email(email).phone(phone)
                .enabled(true).emailVerified(false).phoneVerified(true)
                .accountLocked(false).roles(Set.of(role)).build();
        user.setId(id);
        return user;
    }

    private RegistrationRequest buildRegRequest(String name, String username, String email,
                                                String password, String phone, String otp) {
        RegistrationRequest request = new RegistrationRequest();
        request.setAuthType(AuthType.PHONE_OTP);
        request.setName(name);
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        request.setPhone(phone);
        request.setOtp(otp);
        return request;
    }
}
