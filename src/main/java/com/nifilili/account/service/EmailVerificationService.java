package com.nifilili.account.service;

/**
 * Manages email verification token lifecycle: creation, verification, and resending.
 */
public interface EmailVerificationService {

    /**
     * Creates a verification token and sends a verification email.
     *
     * @param userId user's database ID
     * @param email  user's email address
     */
    void createAndSendVerification(Long userId, String email);

    /**
     * Verifies the email using the provided token.
     *
     * @param token the verification token string
     * @throws com.nifilili.core.exception.InvalidTokenException if token is invalid, expired, or already used
     */
    void verifyEmail(String token);

    /**
     * Resends the verification email for the current user.
     * Invalidates any existing unused tokens before creating a new one.
     *
     * @param userId user's database ID
     * @param email  user's email address
     */
    void resendVerification(Long userId, String email);
}
