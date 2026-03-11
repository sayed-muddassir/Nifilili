package com.nifilili.account.service.impl;

import com.nifilili.account.domain.PasswordResetToken;
import com.nifilili.account.repository.PasswordResetTokenRepository;
import com.nifilili.account.service.AccountSecurityService;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.auth.service.TokenService;
import com.nifilili.core.email.EmailService;
import com.nifilili.core.exception.InvalidPasswordException;
import com.nifilili.core.exception.InvalidTokenException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountSecurityServiceImpl implements AccountSecurityService {

    private static final long LINK_EXPIRY_HOURS = 1;
    private static final long OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PasswordResetTokenRepository resetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenService tokenService;

    @Override
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Revoke all refresh tokens to force re-login on other devices
        tokenService.revokeAllRefreshTokens(userId);
        log.info("Password changed for userId={}", userId);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email, String type) {
        User user = userRepository.findByUsernameOrEmail(email, email).orElse(null);

        // Always return success to avoid email enumeration
        if (user == null) {
            log.debug("Password reset requested for non-existent email, silently ignoring");
            return;
        }

        if ("OTP".equals(type)) {
            String otp = generateOtp();
            PasswordResetToken token = PasswordResetToken.builder()
                    .userId(user.getId())
                    .token(UUID.randomUUID().toString())
                    .otp(otp)
                    .type("OTP")
                    .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                    .used(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            resetTokenRepository.save(token);
            emailService.sendPasswordResetOtp(email, otp);
            log.info("Sent password reset OTP to userId={}", user.getId());
        } else {
            String tokenValue = UUID.randomUUID().toString();
            PasswordResetToken token = PasswordResetToken.builder()
                    .userId(user.getId())
                    .token(tokenValue)
                    .type("LINK")
                    .expiresAt(LocalDateTime.now().plusHours(LINK_EXPIRY_HOURS))
                    .used(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            resetTokenRepository.save(token);
            emailService.sendPasswordResetLink(email, tokenValue);
            log.info("Sent password reset link to userId={}", user.getId());
        }
    }

    @Override
    @Transactional
    public void resetPasswordByLink(String tokenValue, String newPassword) {
        PasswordResetToken token = resetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("Password reset token not found"));

        if (!token.isUsable()) {
            throw new InvalidTokenException("Password reset token is expired or already used");
        }

        completePasswordReset(token, newPassword);
    }

    @Override
    @Transactional
    public void resetPasswordByOtp(String email, String otp, String newPassword) {
        User user = userRepository.findByUsernameOrEmail(email, email)
                .orElseThrow(() -> new InvalidTokenException("Invalid email or OTP"));

        PasswordResetToken token = resetTokenRepository.findByUserIdAndOtpAndUsedFalse(user.getId(), otp)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired OTP"));

        if (!token.isUsable()) {
            throw new InvalidTokenException("OTP is expired or already used");
        }

        completePasswordReset(token, newPassword);
    }

    private void completePasswordReset(PasswordResetToken token, String newPassword) {
        // Mark token as used
        token.setUsed(true);
        resetTokenRepository.save(token);

        // Update password
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + token.getUserId()));

        user.setPassword(passwordEncoder.encode(newPassword));

        // Unlock account if locked (password reset unlocks)
        if (user.isAccountLocked()) {
            user.setAccountLocked(false);
            log.info("Account unlocked via password reset for userId={}", user.getId());
        }

        userRepository.save(user);

        // Revoke all refresh tokens
        tokenService.revokeAllRefreshTokens(user.getId());
        log.info("Password reset completed for userId={}", user.getId());
    }

    private String generateOtp() {
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }
}
