package com.nifilili.auth.events;

import com.nifilili.auth.domain.enums.IdentifierType;
import com.nifilili.auth.domain.enums.OtpPurpose;

/**
 * Published when an OTP is generated and needs to be delivered.
 * Consumed by the messaging module to route delivery via the appropriate channel.
 */
public record OtpRequestedEvent(
        String identifier,
        IdentifierType identifierType,
        String otp,
        OtpPurpose purpose
) {
}
