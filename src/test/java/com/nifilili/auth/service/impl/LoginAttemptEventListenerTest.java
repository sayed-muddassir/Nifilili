package com.nifilili.auth.service.impl;

import com.nifilili.auth.events.LoginAttemptEvent;
import com.nifilili.auth.service.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptEventListenerTest {

    @Mock private LoginAttemptService loginAttemptService;

    @InjectMocks private LoginAttemptEventListener listener;

    // ── onLoginAttempt ────────────────────────────────────────────────────

    @Test
    void onLoginAttempt_WhenEventReceived_ShouldDelegateToService() {
        LoginAttemptEvent event = new LoginAttemptEvent(1L, "john", "192.168.1.1", true);

        listener.onLoginAttempt(event);

        verify(loginAttemptService).recordAttempt(1L, "john", "192.168.1.1", true);
    }

    @Test
    void onLoginAttempt_WhenServiceThrows_ShouldCatchAndLogError() {
        LoginAttemptEvent event = new LoginAttemptEvent(2L, "jane", "10.0.0.1", false);

        doThrow(new RuntimeException("DB error"))
                .when(loginAttemptService).recordAttempt(2L, "jane", "10.0.0.1", false);

        // Should not propagate the exception
        assertDoesNotThrow(() -> listener.onLoginAttempt(event));
        verify(loginAttemptService).recordAttempt(2L, "jane", "10.0.0.1", false);
    }
}
