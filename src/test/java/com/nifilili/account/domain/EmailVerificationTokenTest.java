package com.nifilili.account.domain;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailVerificationTokenTest {

    // ── isExpired ─────────────────────────────────────────────────────────

    @Test
    void isExpired_WhenExpiresAtInFuture_ShouldReturnFalse() {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        assertFalse(token.isExpired());
    }

    @Test
    void isExpired_WhenExpiresAtInPast_ShouldReturnTrue() {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        assertTrue(token.isExpired());
    }

    // ── isUsable ──────────────────────────────────────────────────────────

    @Test
    void isUsable_WhenNotUsedAndNotExpired_ShouldReturnTrue() {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        assertTrue(token.isUsable());
    }

    @Test
    void isUsable_WhenUsedOrExpired_ShouldReturnFalse() {
        // Case 1: used=true, not expired
        EmailVerificationToken usedToken = EmailVerificationToken.builder()
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(true)
                .build();

        assertFalse(usedToken.isUsable(), "Token should not be usable when already used");

        // Case 2: used=false, expired
        EmailVerificationToken expiredToken = EmailVerificationToken.builder()
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();

        assertFalse(expiredToken.isUsable(), "Token should not be usable when expired");

        // Case 3: used=true AND expired
        EmailVerificationToken usedAndExpiredToken = EmailVerificationToken.builder()
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(true)
                .build();

        assertFalse(usedAndExpiredToken.isUsable(), "Token should not be usable when both used and expired");
    }
}
