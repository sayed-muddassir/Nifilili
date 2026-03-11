package com.nifilili.account.service.impl;

import com.nifilili.account.domain.PasswordResetToken;
import com.nifilili.account.repository.PasswordResetTokenRepository;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.auth.service.TokenService;
import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.core.email.EmailService;
import com.nifilili.core.exception.InvalidPasswordException;
import com.nifilili.core.exception.InvalidTokenException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountSecurityServiceImplTest {

    @Mock private PasswordResetTokenRepository resetTokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private TokenService tokenService;

    @InjectMocks private AccountSecurityServiceImpl service;

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    @Test
    void changePassword_WhenCurrentPasswordCorrect_ShouldUpdateAndRevokeTokens() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        User user = User.builder().password("encoded-old").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encoded-new");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.changePassword("oldPass", "newPass");

        assertEquals("encoded-new", user.getPassword());
        verify(tokenService).revokeAllRefreshTokens(1L);
    }

    @Test
    void changePassword_WhenCurrentPasswordWrong_ShouldThrowInvalidPasswordException() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        User user = User.builder().password("encoded-old").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "encoded-old")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> service.changePassword("wrongPass", "newPass"));
    }

    @Test
    void requestPasswordReset_WhenEmailNotFound_ShouldSilentlyReturn() {
        when(userRepository.findByUsernameOrEmail("unknown@test.com", "unknown@test.com"))
                .thenReturn(Optional.empty());

        service.requestPasswordReset("unknown@test.com", "LINK");

        verify(emailService, never()).sendPasswordResetLink(anyString(), anyString());
    }

    @Test
    void requestPasswordReset_WhenTypeLINK_ShouldSendLink() {
        User user = User.builder().build();
        user.setId(1L);
        when(userRepository.findByUsernameOrEmail("john@test.com", "john@test.com"))
                .thenReturn(Optional.of(user));
        when(resetTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.requestPasswordReset("john@test.com", "LINK");

        verify(emailService).sendPasswordResetLink(eq("john@test.com"), anyString());
    }

    @Test
    void requestPasswordReset_WhenTypeOTP_ShouldSendOtp() {
        User user = User.builder().build();
        user.setId(1L);
        when(userRepository.findByUsernameOrEmail("john@test.com", "john@test.com"))
                .thenReturn(Optional.of(user));
        when(resetTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.requestPasswordReset("john@test.com", "OTP");

        verify(emailService).sendPasswordResetOtp(eq("john@test.com"), anyString());
    }

    @Test
    void resetPasswordByLink_WhenTokenValid_ShouldResetAndUnlock() {
        PasswordResetToken token = PasswordResetToken.builder()
                .userId(1L)
                .token("reset-token")
                .type("LINK")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        User user = User.builder().password("old").accountLocked(true).build();
        user.setId(1L);

        when(resetTokenRepository.findByToken("reset-token")).thenReturn(Optional.of(token));
        when(resetTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("encoded-new");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.resetPasswordByLink("reset-token", "newPass");

        assertTrue(token.isUsed());
        assertEquals("encoded-new", user.getPassword());
        assertFalse(user.isAccountLocked());
        verify(tokenService).revokeAllRefreshTokens(1L);
    }

    @Test
    void resetPasswordByLink_WhenTokenExpired_ShouldThrowInvalidTokenException() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("expired")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();

        when(resetTokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        assertThrows(InvalidTokenException.class, () -> service.resetPasswordByLink("expired", "newPass"));
    }
}
