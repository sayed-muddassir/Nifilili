package com.nifilili.auth.service;

import com.nifilili.auth.domain.enums.IdentifierType;
import com.nifilili.auth.domain.enums.OtpPurpose;

/**
 * Manages OTP lifecycle: generation, rate-limit checks, verification,
 * and event publishing for delivery.
 */
public interface OtpService {

    /**
     * Generates an OTP, persists it, and publishes an {@code OtpRequestedEvent}
     * for delivery via the messaging module.
     *
     * @param identifier     phone number or email address
     * @param identifierType PHONE or EMAIL
     * @param purpose        the scenario for which the OTP is generated
     * @throws com.nifilili.core.exception.OtpRateLimitException if rate limit exceeded
     */
    void generateAndSendOtp(String identifier, IdentifierType identifierType, OtpPurpose purpose);

    /**
     * Verifies the OTP against the latest unused token for the identifier and purpose.
     * Increments the attempt counter on failure; invalidates after max attempts.
     *
     * @param identifier phone number or email address
     * @param otp        the 6-digit code entered by the user
     * @param purpose    the scenario being verified
     * @return {@code true} if the OTP is valid
     */
    boolean verifyOtp(String identifier, String otp, OtpPurpose purpose);

    /**
     * Checks whether the identifier has exceeded the OTP request rate limit.
     *
     * @param identifier phone number or email address
     * @param purpose    the OTP purpose
     * @return {@code true} if rate-limited (should block the request)
     */
    boolean isRateLimited(String identifier, OtpPurpose purpose);
}
