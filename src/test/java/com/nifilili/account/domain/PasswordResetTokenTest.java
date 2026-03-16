package com.nifilili.account.domain;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenTest {

    // ── isExpired ─────────────────────────────────────────────────────────

    @Test
    void isExpired_WhenExpiresAtInFuture_ShouldReturnFalse() {
        PasswordResetToken token = PasswordResetToken.builder()
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        assertFalse(token.isExpired());
    }

    @Test
    void isExpired_WhenExpiresAtInPast_ShouldReturnTrue() {
        PasswordResetToken token = PasswordResetToken.builder()
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        assertTrue(token.isExpired());
    }

    // ── isUsable ──────────────────────────────────────────────────────────

    @Test
    void isUsable_WhenNotUsedAndNotExpired_ShouldReturnTrue() {
        PasswordResetToken token = PasswordResetToken.builder()
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        assertTrue(token.isUsable());
    }

    @Test
    void isUsable_WhenUsedOrExpired_ShouldReturnFalse() {
        // Case 1: used=true, not expired
        PasswordResetToken usedToken = PasswordResetToken.builder()
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(true)
                .build();

        assertFalse(usedToken.isUsable(), "Token should not be usable when already used");

        // Case 2: used=false, expired
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();

        assertFalse(expiredToken.isUsable(), "Token should not be usable when expired");

        // Case 3: used=true AND expired
        PasswordResetToken usedAndExpiredToken = PasswordResetToken.builder()
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(true)
                .build();

        assertFalse(usedAndExpiredToken.isUsable(), "Token should not be usable when both used and expired");
    }
}
