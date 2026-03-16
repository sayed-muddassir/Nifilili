package com.nifilili.auth.domain.enums;

/**
 * Authentication provider types for linked identity support.
 * Each user can have at most one entry per provider type.
 */
public enum ProviderType {

    /** Local email + password authentication. */
    LOCAL_EMAIL,

    /** Local phone + OTP authentication. */
    LOCAL_PHONE,

    /** Google OAuth 2.0 (future). */
    GOOGLE,

    /** Facebook OAuth 2.0 (future). */
    FACEBOOK
}
