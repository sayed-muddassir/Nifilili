package com.nifilili.account.events;

/**
 * Published after a new user registers. Account module listens to trigger
 * email verification token generation and verification email sending.
 */
public record UserRegisteredEvent(Long userId, String email) {
}
