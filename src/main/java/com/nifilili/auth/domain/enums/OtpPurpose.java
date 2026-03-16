package com.nifilili.auth.domain.enums;

/**
 * Purpose for which an OTP was generated.
 * Determines expiry duration and validation logic.
 */
public enum OtpPurpose {

    /** OTP for logging into an existing account. */
    LOGIN,

    /** OTP for verifying phone/email during registration. */
    SIGNUP,

    /** OTP for resetting a forgotten password. */
    PASSWORD_RESET,

    /** OTP for verifying a phone number (identity linking). */
    VERIFY_PHONE,

    /** OTP for verifying an email address (identity linking). */
    VERIFY_EMAIL
}
