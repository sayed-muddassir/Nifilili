package com.nifilili.account.service.impl;

import com.nifilili.account.domain.EmailVerificationToken;
import com.nifilili.account.repository.EmailVerificationTokenRepository;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.core.email.EmailService;
import com.nifilili.core.exception.InvalidTokenException;
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
}
