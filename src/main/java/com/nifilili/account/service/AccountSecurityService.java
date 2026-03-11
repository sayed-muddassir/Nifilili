package com.nifilili.account.service;

/**
 * Manages account security operations: password changes, password resets (link and OTP).
 */
public interface AccountSecurityService {

    /**
     * Changes the authenticated user's password.
     *
     * @param currentPassword the user's current password for verification
     * @param newPassword     the new password to set
     * @throws com.nifilili.core.exception.InvalidPasswordException if current password is incorrect
     */
    void changePassword(String currentPassword, String newPassword);

    /**
     * Initiates a password reset by sending a link or OTP to the user's email.
     *
     * @param email the email address of the account
     * @param type  "LINK" for tokenized link (1h) or "OTP" for 6-digit code (10min)
     */
    void requestPasswordReset(String email, String type);

    /**
     * Resets the password using a tokenized link.
     *
     * @param token       the reset token from the email link
     * @param newPassword the new password to set
     * @throws com.nifilili.core.exception.InvalidTokenException if token is invalid, expired, or used
     */
    void resetPasswordByLink(String token, String newPassword);

    /**
     * Resets the password using an OTP code.
     *
     * @param email       the email address of the account
     * @param otp         the 6-digit OTP from the email
     * @param newPassword the new password to set
     * @throws com.nifilili.core.exception.InvalidTokenException if OTP is invalid, expired, or used
     */
    void resetPasswordByOtp(String email, String otp, String newPassword);
}
