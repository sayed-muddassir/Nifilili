package com.nifilili.auth.service.impl;

import com.nifilili.auth.config.OtpProperties;
import com.nifilili.auth.domain.OtpToken;
import com.nifilili.auth.domain.enums.IdentifierType;
import com.nifilili.auth.domain.enums.OtpPurpose;
import com.nifilili.auth.events.OtpRequestedEvent;
import com.nifilili.auth.repository.OtpTokenRepository;
import com.nifilili.auth.service.OtpService;
import com.nifilili.core.exception.OtpRateLimitException;
import com.nifilili.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OtpTokenRepository otpTokenRepository;
    private final OtpProperties otpProperties;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void generateAndSendOtp(String identifier, IdentifierType identifierType, OtpPurpose purpose) {
        if (isRateLimited(identifier, purpose)) {
            throw new OtpRateLimitException(
                    "Too many OTP requests. Please try again in " + otpProperties.getRateLimit().getWindowMinutes() + " minutes.");
        }

        String otpCode = generateOtpCode();
        int expiryMinutes = resolveExpiryMinutes(purpose);

        OtpToken token = OtpToken.builder()
                .identifier(identifier)
                .identifierType(identifierType)
                .otp(otpCode)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        otpTokenRepository.save(token);
        log.debug("Generated OTP for identifier='{}' purpose={} expiresInMin={}", identifier, purpose, expiryMinutes);

        eventPublisher.publishEvent(new OtpRequestedEvent(identifier, identifierType, otpCode, purpose));
        log.info("Published OtpRequestedEvent for identifier='{}' purpose={}", identifier, purpose);
    }

    @Override
    @Transactional
    public boolean verifyOtp(String identifier, String otp, OtpPurpose purpose) {
        OtpToken token = otpTokenRepository
                .findFirstByIdentifierAndOtpAndPurposeAndUsedFalseOrderByCreatedAtDesc(identifier, otp, purpose)
                .orElseThrow(() -> new ResourceNotFoundException("OTP not found or already used for this identifier and purpose."));

        if (!token.isUsable()) {
            log.info("OTP verification failed: no valid token for identifier='{}' purpose={}", identifier, purpose);
            return false;
        }

        if (token.getAttempts() >= otpProperties.getMaxAttempts()) {
            token.setUsed(true); // Invalidate after max attempts
            otpTokenRepository.save(token);
            log.warn("OTP max attempts exceeded for identifier='{}' purpose={}", identifier, purpose);
            return false;
        }

        // Mark as used on successful verification
        token.setUsed(true);
        otpTokenRepository.save(token);
        log.info("OTP verified successfully for identifier='{}' purpose={}", identifier, purpose);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRateLimited(String identifier, OtpPurpose purpose) {
        OtpProperties.RateLimit rateLimit = otpProperties.getRateLimit();
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(rateLimit.getWindowMinutes());

        long recentCount = otpTokenRepository.countByIdentifierAndPurposeAndCreatedAtAfter(
                identifier, purpose, windowStart);

        return recentCount >= rateLimit.getMaxRequests();
    }

    private String generateOtpCode() {
        String defaultCode = otpProperties.getDefaultCode();
        if (defaultCode != null && !defaultCode.isBlank()) {
            return defaultCode;
        }
        int bound = (int) Math.pow(10, otpProperties.getLength());
        int min = bound / 10;
        int code = min + SECURE_RANDOM.nextInt(bound - min);
        return String.valueOf(code);
    }

    private int resolveExpiryMinutes(OtpPurpose purpose) {
        OtpProperties.ExpiryMinutes expiry = otpProperties.getExpiryMinutes();
        return switch (purpose) {
            case LOGIN -> expiry.getLogin();
            case SIGNUP -> expiry.getSignup();
            case PASSWORD_RESET -> expiry.getPasswordReset();
            case VERIFY_PHONE -> expiry.getVerifyPhone();
            case VERIFY_EMAIL -> expiry.getVerifyEmail();
        };
    }
}
