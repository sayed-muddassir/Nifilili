package com.nifilili.core.exception;

/**
 * Thrown when OTP request rate limit is exceeded for an identifier.
 */
public class OtpRateLimitException extends RuntimeException {

    public OtpRateLimitException(String message) {
        super(message);
    }
}
