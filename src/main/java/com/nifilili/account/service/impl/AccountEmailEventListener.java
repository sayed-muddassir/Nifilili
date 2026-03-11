package com.nifilili.account.service.impl;

import com.nifilili.account.events.UserRegisteredEvent;
import com.nifilili.account.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens to user registration events and triggers email verification.
 * Follows the established cross-module event listener pattern (see BusinessKycEventListener).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountEmailEventListener {

    private final EmailVerificationService emailVerificationService;

    @EventListener
    @Transactional
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("User registered event received for userId={}, sending verification email", event.userId());
        emailVerificationService.createAndSendVerification(event.userId(), event.email());
    }
}
