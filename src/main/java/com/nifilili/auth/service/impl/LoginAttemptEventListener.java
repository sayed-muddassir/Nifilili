package com.nifilili.auth.service.impl;

import com.nifilili.auth.events.LoginAttemptEvent;
import com.nifilili.auth.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens to user login attempt events and triggers record attempt in DB.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptEventListener {

    private final LoginAttemptService loginAttemptService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLoginAttempt(LoginAttemptEvent event) {
        log.info("Login attempt event received for userId={}, username={}, success={}",
                event.userId(), event.username(), event.success());
        try {
            loginAttemptService.recordAttempt(
                    event.userId(),
                    event.username(),
                    event.ipAddress(),
                    event.success()
            );
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
