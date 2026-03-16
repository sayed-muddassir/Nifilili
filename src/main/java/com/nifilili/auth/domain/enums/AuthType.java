package com.nifilili.auth.domain.enums;

/**
 * Supported authentication methods. Each value corresponds to an
 * {@link com.nifilili.auth.service.AuthProviderStrategy} implementation.
 */
public enum AuthType {

    /** Traditional email/username + password authentication. */
    EMAIL_PASSWORD,

    /** Phone number + one-time password authentication. */
    PHONE_OTP,

    /** Google OAuth 2.0 social login (future). */
    GOOGLE,

    /** Facebook OAuth 2.0 social login (future). */
    FACEBOOK
}
