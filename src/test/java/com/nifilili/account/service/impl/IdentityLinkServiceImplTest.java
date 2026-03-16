package com.nifilili.account.service.impl;

import com.nifilili.auth.domain.User;
import com.nifilili.auth.domain.enums.IdentifierType;
import com.nifilili.auth.domain.enums.OtpPurpose;
import com.nifilili.auth.domain.enums.ProviderType;
import com.nifilili.auth.repository.UserAuthProviderRepository;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.auth.service.OtpService;
import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.core.exception.EmailAlreadyExistsException;
import com.nifilili.core.exception.InvalidTokenException;
import com.nifilili.core.exception.PhoneAlreadyExistsException;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityLinkServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserAuthProviderRepository authProviderRepository;
    @Mock private OtpService otpService;

    @InjectMocks private IdentityLinkServiceImpl identityLinkService;

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // ── initiateLinkPhone ────────────────────────────────────────────────

    @Test
    void initiateLinkPhone_WhenPhoneNotRegistered_ShouldSendOtp() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        when(userRepository.existsByPhone("+977-9800000000")).thenReturn(false);

        identityLinkService.initiateLinkPhone("+977-9800000000");

        verify(otpService).generateAndSendOtp("+977-9800000000", IdentifierType.PHONE, OtpPurpose.VERIFY_PHONE);
    }

    @Test
    void initiateLinkPhone_WhenPhoneAlreadyRegistered_ShouldThrowPhoneAlreadyExistsException() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        when(userRepository.existsByPhone("+977-9800000000")).thenReturn(true);

        assertThrows(PhoneAlreadyExistsException.class,
                () -> identityLinkService.initiateLinkPhone("+977-9800000000"));

        verify(otpService, never()).generateAndSendOtp(any(), any(), any());
    }

    // ── completeLinkPhone ────────────────────────────────────────────────

    @Test
    void completeLinkPhone_WhenValidOtp_ShouldLinkPhoneAndCreateProvider() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);

        User user = User.builder().name("Test").username("test").build();
        user.setId(1L);

        when(otpService.verifyOtp("+977-9800000000", "123456", OtpPurpose.VERIFY_PHONE)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(authProviderRepository.existsByUserIdAndProviderType(1L, ProviderType.LOCAL_PHONE)).thenReturn(false);

        identityLinkService.completeLinkPhone("+977-9800000000", "123456");

        assertEquals("+977-9800000000", user.getPhone());
        assertTrue(user.isPhoneVerified());
        verify(userRepository).save(user);
        verify(authProviderRepository).save(any());
    }

    @Test
    void completeLinkPhone_WhenInvalidOtp_ShouldThrowInvalidTokenException() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        when(otpService.verifyOtp("+977-9800000000", "999999", OtpPurpose.VERIFY_PHONE)).thenReturn(false);

        assertThrows(InvalidTokenException.class,
                () -> identityLinkService.completeLinkPhone("+977-9800000000", "999999"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void completeLinkPhone_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        when(otpService.verifyOtp("+977-9800000000", "123456", OtpPurpose.VERIFY_PHONE)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> identityLinkService.completeLinkPhone("+977-9800000000", "123456"));
    }

    @Test
    void completeLinkPhone_WhenProviderAlreadyExists_ShouldNotCreateDuplicate() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);

        User user = User.builder().name("Test").username("test").build();
        user.setId(1L);

        when(otpService.verifyOtp("+977-9800000000", "123456", OtpPurpose.VERIFY_PHONE)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(authProviderRepository.existsByUserIdAndProviderType(1L, ProviderType.LOCAL_PHONE)).thenReturn(true);

        identityLinkService.completeLinkPhone("+977-9800000000", "123456");

        verify(userRepository).save(user);
        verify(authProviderRepository, never()).save(any());
    }

    // ── initiateLinkEmail ────────────────────────────────────────────────

    @Test
    void initiateLinkEmail_WhenEmailNotRegistered_ShouldSendOtp() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        identityLinkService.initiateLinkEmail("new@example.com");

        verify(otpService).generateAndSendOtp("new@example.com", IdentifierType.EMAIL, OtpPurpose.VERIFY_EMAIL);
    }

    @Test
    void initiateLinkEmail_WhenEmailAlreadyRegistered_ShouldThrowEmailAlreadyExistsException() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> identityLinkService.initiateLinkEmail("taken@example.com"));

        verify(otpService, never()).generateAndSendOtp(any(), any(), any());
    }

    // ── completeLinkEmail ────────────────────────────────────────────────

    @Test
    void completeLinkEmail_WhenValidOtp_ShouldLinkEmailAndCreateProvider() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);

        User user = User.builder().name("Test").username("test").build();
        user.setId(1L);

        when(otpService.verifyOtp("new@example.com", "123456", OtpPurpose.VERIFY_EMAIL)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(authProviderRepository.existsByUserIdAndProviderType(1L, ProviderType.LOCAL_EMAIL)).thenReturn(false);

        identityLinkService.completeLinkEmail("new@example.com", "123456");

        assertEquals("new@example.com", user.getEmail());
        assertTrue(user.isEmailVerified());
        verify(userRepository).save(user);
        verify(authProviderRepository).save(any());
    }

    @Test
    void completeLinkEmail_WhenInvalidOtp_ShouldThrowInvalidTokenException() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        when(otpService.verifyOtp("new@example.com", "999999", OtpPurpose.VERIFY_EMAIL)).thenReturn(false);

        assertThrows(InvalidTokenException.class,
                () -> identityLinkService.completeLinkEmail("new@example.com", "999999"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void completeLinkEmail_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        when(otpService.verifyOtp("new@example.com", "123456", OtpPurpose.VERIFY_EMAIL)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> identityLinkService.completeLinkEmail("new@example.com", "123456"));
    }

    @Test
    void completeLinkEmail_WhenProviderAlreadyExists_ShouldNotCreateDuplicateProvider() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);

        User user = User.builder().name("Test").username("test").build();
        user.setId(1L);

        when(otpService.verifyOtp("new@example.com", "123456", OtpPurpose.VERIFY_EMAIL)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(authProviderRepository.existsByUserIdAndProviderType(1L, ProviderType.LOCAL_EMAIL)).thenReturn(true);

        identityLinkService.completeLinkEmail("new@example.com", "123456");

        verify(userRepository).save(user);
        verify(authProviderRepository, never()).save(any());
    }
}
