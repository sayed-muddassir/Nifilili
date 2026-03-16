package com.nifilili.account.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nifilili.account.events.UserRegisteredEvent;
import com.nifilili.account.service.EmailVerificationService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountEmailEventListenerTest {

    @Mock private EmailVerificationService emailVerificationService;

    @InjectMocks private AccountEmailEventListener listener;

    // ── onUserRegistered ─────────────────────────────────────────────────

    @Test
    void onUserRegistered_WhenEventReceived_ShouldDelegateToVerificationService() {
        UserRegisteredEvent event = new UserRegisteredEvent(1L, "jane@example.com");

        listener.onUserRegistered(event);

        verify(emailVerificationService).createAndSendVerification(1L, "jane@example.com");
        verifyNoMoreInteractions(emailVerificationService);
    }

    @Test
    void onUserRegistered_WhenServiceThrows_ShouldPropagateException() {
        UserRegisteredEvent event = new UserRegisteredEvent(2L, "error@example.com");
        doThrow(new RuntimeException("SMTP failure"))
                .when(emailVerificationService).createAndSendVerification(2L, "error@example.com");

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> listener.onUserRegistered(event)
        );

        assertEquals("SMTP failure", ex.getMessage());
        verify(emailVerificationService).createAndSendVerification(2L, "error@example.com");
    }
}
