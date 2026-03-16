package com.nifilili.auth.service.impl;

import com.nifilili.auth.config.OtpProperties;
import com.nifilili.auth.domain.OtpToken;
import com.nifilili.auth.domain.enums.IdentifierType;
import com.nifilili.auth.domain.enums.OtpPurpose;
import com.nifilili.auth.events.OtpRequestedEvent;
import com.nifilili.auth.repository.OtpTokenRepository;
import com.nifilili.core.exception.OtpRateLimitException;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock private OtpTokenRepository otpTokenRepository;
    @Mock private OtpProperties otpProperties;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Captor private ArgumentCaptor<OtpToken> otpTokenCaptor;
    @Captor private ArgumentCaptor<OtpRequestedEvent> eventCaptor;

    @InjectMocks private OtpServiceImpl otpService;

    private OtpProperties.ExpiryMinutes expiryMinutes;
    private OtpProperties.RateLimit rateLimit;

    @BeforeEach
    void setUp() {
        expiryMinutes = new OtpProperties.ExpiryMinutes();
        expiryMinutes.setLogin(5);
        expiryMinutes.setSignup(10);
        expiryMinutes.setPasswordReset(10);
        expiryMinutes.setVerifyPhone(10);
        expiryMinutes.setVerifyEmail(1440);

        rateLimit = new OtpProperties.RateLimit();
        rateLimit.setMaxRequests(3);
        rateLimit.setWindowMinutes(5);
    }

    @Test
    void generateAndSendOtp_WhenNotRateLimited_ShouldSaveTokenAndPublishEvent() {
        when(otpProperties.getRateLimit()).thenReturn(rateLimit);
        when(otpProperties.getDefaultCode()).thenReturn("123456");
        when(otpProperties.getExpiryMinutes()).thenReturn(expiryMinutes);
        when(otpTokenRepository.countByIdentifierAndPurposeAndCreatedAtAfter(anyString(), any(), any()))
                .thenReturn(0L);
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(inv -> inv.getArgument(0));

        otpService.generateAndSendOtp("+977-9800000000", IdentifierType.PHONE, OtpPurpose.LOGIN);

        verify(otpTokenRepository).save(otpTokenCaptor.capture());
        OtpToken saved = otpTokenCaptor.getValue();
        assertEquals("+977-9800000000", saved.getIdentifier());
        assertEquals(IdentifierType.PHONE, saved.getIdentifierType());
        assertEquals("123456", saved.getOtp());
        assertEquals(OtpPurpose.LOGIN, saved.getPurpose());
        assertFalse(saved.isUsed());
        assertEquals(0, saved.getAttempts());

        verify(eventPublisher).publishEvent(eventCaptor.capture());
        OtpRequestedEvent event = eventCaptor.getValue();
        assertEquals("+977-9800000000", event.identifier());
        assertEquals(IdentifierType.PHONE, event.identifierType());
        assertEquals("123456", event.otp());
        assertEquals(OtpPurpose.LOGIN, event.purpose());
    }

    @Test
    void generateAndSendOtp_WhenRateLimited_ShouldThrowOtpRateLimitException() {
        when(otpProperties.getRateLimit()).thenReturn(rateLimit);
        when(otpTokenRepository.countByIdentifierAndPurposeAndCreatedAtAfter(anyString(), any(), any()))
                .thenReturn(3L);

        assertThrows(OtpRateLimitException.class,
                () -> otpService.generateAndSendOtp("+977-9800000000", IdentifierType.PHONE, OtpPurpose.LOGIN));

        verify(otpTokenRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void generateAndSendOtp_WhenNoDefaultCode_ShouldGenerateRandomOtp() {
        when(otpProperties.getRateLimit()).thenReturn(rateLimit);
        when(otpProperties.getDefaultCode()).thenReturn(null);
        when(otpProperties.getLength()).thenReturn(6);
        when(otpProperties.getExpiryMinutes()).thenReturn(expiryMinutes);
        when(otpTokenRepository.countByIdentifierAndPurposeAndCreatedAtAfter(anyString(), any(), any()))
                .thenReturn(0L);
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(inv -> inv.getArgument(0));

        otpService.generateAndSendOtp("test@example.com", IdentifierType.EMAIL, OtpPurpose.SIGNUP);

        verify(otpTokenRepository).save(otpTokenCaptor.capture());
        OtpToken saved = otpTokenCaptor.getValue();
        assertNotNull(saved.getOtp());
        assertEquals(6, saved.getOtp().length());
        assertTrue(saved.getOtp().matches("\\d{6}"));
    }

    @Test
    void verifyOtp_WhenValidOtp_ShouldReturnTrue() {
        OtpToken token = OtpToken.builder()
                .identifier("+977-9800000000")
                .otp("123456")
                .purpose(OtpPurpose.LOGIN)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .attempts(0)
                .build();

        when(otpProperties.getMaxAttempts()).thenReturn(3);
        when(otpTokenRepository.findFirstByIdentifierAndOtpAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                "+977-9800000000", "123456", OtpPurpose.LOGIN))
                .thenReturn(Optional.of(token));

        boolean result = otpService.verifyOtp("+977-9800000000", "123456", OtpPurpose.LOGIN);

        assertTrue(result);
        assertTrue(token.isUsed());
        verify(otpTokenRepository).save(token);
    }

    @Test
    void verifyOtp_WhenExpiredOtp_ShouldReturnFalse() {
        OtpToken token = OtpToken.builder()
                .identifier("+977-9800000000")
                .otp("123456")
                .purpose(OtpPurpose.LOGIN)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .attempts(0)
                .build();

        when(otpTokenRepository.findFirstByIdentifierAndOtpAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                "+977-9800000000", "123456", OtpPurpose.LOGIN))
                .thenReturn(Optional.of(token));

        boolean result = otpService.verifyOtp("+977-9800000000", "123456", OtpPurpose.LOGIN);

        assertFalse(result);
    }

    @Test
    void verifyOtp_WhenOtpNotFound_ShouldReturnFalse() {
        when(otpTokenRepository.findFirstByIdentifierAndOtpAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                anyString(), anyString(), any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                otpService.verifyOtp("+977-9800000000", "999999", OtpPurpose.LOGIN));
    }

    @Test
    void verifyOtp_WhenMaxAttemptsExceeded_ShouldReturnFalse() {
        OtpToken token = OtpToken.builder()
                .identifier("+977-9800000000")
                .otp("123456")
                .purpose(OtpPurpose.LOGIN)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .attempts(3)
                .build();

        when(otpProperties.getMaxAttempts()).thenReturn(3);
        when(otpTokenRepository.findFirstByIdentifierAndOtpAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                "+977-9800000000", "123456", OtpPurpose.LOGIN))
                .thenReturn(Optional.of(token));

        boolean result = otpService.verifyOtp("+977-9800000000", "123456", OtpPurpose.LOGIN);

        assertFalse(result);
    }

    @Test
    void isRateLimited_WhenBelowLimit_ShouldReturnFalse() {
        when(otpProperties.getRateLimit()).thenReturn(rateLimit);
        when(otpTokenRepository.countByIdentifierAndPurposeAndCreatedAtAfter(anyString(), any(), any()))
                .thenReturn(2L);

        boolean result = otpService.isRateLimited("+977-9800000000", OtpPurpose.LOGIN);

        assertFalse(result);
    }

    @Test
    void isRateLimited_WhenAtLimit_ShouldReturnTrue() {
        when(otpProperties.getRateLimit()).thenReturn(rateLimit);
        when(otpTokenRepository.countByIdentifierAndPurposeAndCreatedAtAfter(anyString(), any(), any()))
                .thenReturn(3L);

        boolean result = otpService.isRateLimited("+977-9800000000", OtpPurpose.LOGIN);

        assertTrue(result);
    }
}
