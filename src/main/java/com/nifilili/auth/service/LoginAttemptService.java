package com.nifilili.auth.service;

/**
 * Tracks login attempts and enforces account lockout policy.
 * Lockout policy: 5 consecutive failed attempts locks the account until password reset.
 */
public interface LoginAttemptService {

    /**
     * Records a login attempt (success or failure).
     *
     * @param userId    user ID (may be null if user not found)
     * @param username  the username/email used to attempt login
     * @param ipAddress client IP
     * @param success   whether authentication succeeded
     */
    void recordAttempt(Long userId, String username, String ipAddress, boolean success);

    /**
     * Checks if the account associated with the username is locked due to failed attempts.
     *
     * @param username the username/email
     * @return true if the account is locked
     */
    boolean isAccountLocked(String username);
}
