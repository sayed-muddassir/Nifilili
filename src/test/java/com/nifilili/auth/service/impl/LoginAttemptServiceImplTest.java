package com.nifilili.auth.service.impl;

import com.nifilili.auth.domain.LoginAttempt;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.LoginAttemptRepository;
import com.nifilili.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceImplTest {

    @Mock private LoginAttemptRepository loginAttemptRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private LoginAttemptServiceImpl loginAttemptService;

    @Test
    void recordAttempt_WhenSuccessful_ShouldPersistSuccessAttempt() {
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        loginAttemptService.recordAttempt(1L, "john", "192.168.1.1", true);

        ArgumentCaptor<LoginAttempt> captor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(loginAttemptRepository).save(captor.capture());
        assertTrue(captor.getValue().isSuccess());
        assertEquals("john", captor.getValue().getUsername());
    }

    @Test
    void recordAttempt_WhenFifthFailure_ShouldLockAccount() {
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loginAttemptRepository.countRecentFailedAttempts("john")).thenReturn(5L);

        User user = User.builder().enabled(true).accountLocked(false).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        loginAttemptService.recordAttempt(1L, "john", "192.168.1.1", false);

        assertTrue(user.isAccountLocked());
        verify(userRepository).save(user);
    }

    @Test
    void recordAttempt_WhenBelowThreshold_ShouldNotLockAccount() {
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loginAttemptRepository.countRecentFailedAttempts("john")).thenReturn(3L);

        loginAttemptService.recordAttempt(1L, "john", "192.168.1.1", false);

        verify(userRepository, never()).save(any());
    }

    @Test
    void isAccountLocked_WhenLocked_ShouldReturnTrue() {
        User user = User.builder().accountLocked(true).build();
        when(userRepository.findByUsernameOrEmail("john", "john")).thenReturn(Optional.of(user));

        assertTrue(loginAttemptService.isAccountLocked("john"));
    }

    @Test
    void isAccountLocked_WhenNotLocked_ShouldReturnFalse() {
        User user = User.builder().accountLocked(false).build();
        when(userRepository.findByUsernameOrEmail("john", "john")).thenReturn(Optional.of(user));

        assertFalse(loginAttemptService.isAccountLocked("john"));
    }

    @Test
    void recordAttempt_WhenFailedButUserIdNull_ShouldNotCheckLockThreshold() {
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        loginAttemptService.recordAttempt(null, "unknown", "192.168.1.1", false);

        verify(loginAttemptRepository).save(any(LoginAttempt.class));
        verify(loginAttemptRepository, never()).countRecentFailedAttempts(any());
    }

    @Test
    void isAccountLocked_WhenUserNotFound_ShouldReturnFalse() {
        when(userRepository.findByUsernameOrEmail("ghost", "ghost")).thenReturn(Optional.empty());

        assertFalse(loginAttemptService.isAccountLocked("ghost"));
    }

    @Test
    void recordAttempt_WhenAccountAlreadyLocked_ShouldNotSaveAgain() {
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loginAttemptRepository.countRecentFailedAttempts("john")).thenReturn(5L);

        User user = User.builder().enabled(true).accountLocked(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        loginAttemptService.recordAttempt(1L, "john", "192.168.1.1", false);

        // Account already locked, should not save user again
        verify(userRepository, never()).save(any(User.class));
    }
}
