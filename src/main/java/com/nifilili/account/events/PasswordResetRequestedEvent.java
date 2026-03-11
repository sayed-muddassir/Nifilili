package com.nifilili.account.events;

/**
 * Published when a user requests a password reset.
 * Can be consumed for audit logging or rate-limiting purposes.
 */
public record PasswordResetRequestedEvent(Long userId, String email, String type) {
}
