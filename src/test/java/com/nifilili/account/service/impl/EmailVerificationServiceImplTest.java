package com.nifilili.account.service.impl;

import com.nifilili.account.domain.EmailVerificationToken;
import com.nifilili.account.repository.EmailVerificationTokenRepository;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.core.email.EmailService;
import com.nifilili.core.exception.InvalidTokenException;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    @Mock private EmailVerificationTokenRepository tokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;

    @InjectMocks private EmailVerificationServiceImpl service;

    @Test
    void createAndSendVerification_WhenCalled_ShouldPersistTokenAndSendEmail() {
        when(tokenRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createAndSendVerification(1L, "john@test.com");

        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        assertNotNull(captor.getValue().getToken());
        assertEquals(1L, captor.getValue().getUserId());
        assertFalse(captor.getValue().isUsed());

        verify(emailService).sendVerificationEmail(eq("john@test.com"), anyString());
    }

    @Test
    void verifyEmail_WhenTokenValid_ShouldMarkUserEmailVerified() {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .userId(1L)
                .token("valid-token")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        User user = User.builder().emailVerified(false).build();

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.verifyEmail("valid-token");

        assertTrue(token.isUsed());
        assertTrue(user.isEmailVerified());
    }

    @Test
    void verifyEmail_WhenTokenExpired_ShouldThrowInvalidTokenException() {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .userId(1L)
                .token("expired-token")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(false)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThrows(InvalidTokenException.class, () -> service.verifyEmail("expired-token"));
    }

    @Test
    void verifyEmail_WhenTokenNotFound_ShouldThrowInvalidTokenException() {
        when(tokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> service.verifyEmail("nonexistent"));
    }

    @Test
    void resendVerification_WhenUserNotVerified_ShouldDelegateToCreateAndSend() {
        User user = User.builder().emailVerified(false).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service.resendVerification(1L, "john@test.com");

        verify(emailService).sendVerificationEmail(eq("john@test.com"), anyString());
    }

    @Test
    void resendVerification_WhenUserAlreadyVerified_ShouldReturnWithoutSending() {
        User user = User.builder().emailVerified(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        service.resendVerification(1L, "john@test.com");

        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void resendVerification_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.resendVerification(99L, "john@test.com"));
    }

    @Test
    void createAndSendVerification_WhenExistingUnusedTokenExists_ShouldInvalidateOldToken() {
        EmailVerificationToken existing = EmailVerificationToken.builder()
                .userId(1L).token("old-token").expiresAt(LocalDateTime.now().plusHours(24))
                .used(false).createdAt(LocalDateTime.now().minusHours(1)).build();

        when(tokenRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(existing));
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createAndSendVerification(1L, "john@test.com");

        assertTrue(existing.isUsed());
        verify(tokenRepository, times(2)).save(any(EmailVerificationToken.class));
        verify(emailService).sendVerificationEmail(eq("john@test.com"), anyString());
    }

    @Test
    void verifyEmail_WhenTokenAlreadyUsed_ShouldThrowInvalidTokenException() {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .userId(1L).token("used-token").expiresAt(LocalDateTime.now().plusHours(24))
                .used(true).createdAt(LocalDateTime.now()).build();

        when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));

        assertThrows(InvalidTokenException.class, () -> service.verifyEmail("used-token"));
    }
}
