package com.nifilili.core.email.impl;

import com.nifilili.core.email.EmailMessage;
import com.nifilili.core.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * Development/testing email service that logs email content instead of sending.
 * Automatically replaced when a real EmailService bean is registered with @Primary.
 */
@Service
@ConditionalOnMissingBean(value = EmailService.class, ignored = LoggingEmailService.class)
@Slf4j
public class LoggingEmailService implements EmailService {

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("  EMAIL VERIFICATION");
        log.info("  To: {}", toEmail);
        log.info("  Token: {}", token);
        log.info("  Link: http://localhost:3000/verify-email?token={}", token);
        log.info("═══════════════════════════════════════════════════════════");
    }

    @Override
    public void sendPasswordResetLink(String toEmail, String token) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("  PASSWORD RESET (LINK)");
        log.info("  To: {}", toEmail);
        log.info("  Token: {}", token);
        log.info("  Link: http://localhost:3000/reset-password?token={}", token);
        log.info("═══════════════════════════════════════════════════════════");
    }

    @Override
    public void sendPasswordResetOtp(String toEmail, String otp) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("  PASSWORD RESET (OTP)");
        log.info("  To: {}", toEmail);
        log.info("  OTP: {}", otp);
        log.info("═══════════════════════════════════════════════════════════");
    }

    @Override
    public void send(EmailMessage message) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("  GENERIC EMAIL");
        log.info("  To: {}", message.getTo());
        log.info("  Subject: {}", message.getSubject());
        log.info("  Body: {}", message.getBody());
        log.info("  HTML: {}", message.isHtml());
        log.info("═══════════════════════════════════════════════════════════");
    }
}
