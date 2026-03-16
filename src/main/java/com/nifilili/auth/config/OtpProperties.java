package com.nifilili.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized OTP configuration. Mapped from {@code app.otp.*} in application.yml.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.otp")
public class OtpProperties {

    /** Fixed OTP code for development. Set to null for random generation. */
    private String defaultCode = "123456";

    /** OTP code length (used when defaultCode is null). */
    private int length = 6;

    /** Maximum verification attempts before the OTP is invalidated. */
    private int maxAttempts = 3;

    /** Expiry durations in minutes per OTP purpose. */
    private ExpiryMinutes expiryMinutes = new ExpiryMinutes();

    /** Rate limiting configuration. */
    private RateLimit rateLimit = new RateLimit();

    @Getter
    @Setter
    public static class ExpiryMinutes {
        private int login = 5;
        private int signup = 10;
        private int passwordReset = 10;
        private int verifyPhone = 10;
        private int verifyEmail = 1440;
    }

    @Getter
    @Setter
    public static class RateLimit {
        /** Max OTP requests per identifier per window. */
        private int maxRequests = 3;
        /** Rate limit window in minutes. */
        private int windowMinutes = 5;
    }
}
