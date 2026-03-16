package com.nifilili.account.validation;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.core.exception.EmailNotVerifiedException;
import com.nifilili.core.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerifiedGuardTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private EmailVerifiedGuard guard;

    // ── requireVerified ──────────────────────────────────────────────────

    @Test
    void requireVerified_WhenEmailVerified_ShouldNotThrow() {
        User user = User.builder().emailVerified(true).build();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> guard.requireVerified(1L));

        verify(userRepository).findById(1L);
    }

    @Test
    void requireVerified_WhenEmailNotVerified_ShouldThrowEmailNotVerifiedException() {
        User user = User.builder().emailVerified(false).build();
        user.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        EmailNotVerifiedException ex = assertThrows(
                EmailNotVerifiedException.class,
                () -> guard.requireVerified(2L)
        );

        assertTrue(ex.getMessage().contains("Email verification is required"));
        verify(userRepository).findById(2L);
    }

    @Test
    void requireVerified_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> guard.requireVerified(999L)
        );

        assertTrue(ex.getMessage().contains("User not found: 999"));
        verify(userRepository).findById(999L);
    }
}
