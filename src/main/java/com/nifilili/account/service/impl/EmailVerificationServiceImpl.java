package com.nifilili.account.service.impl;

import com.nifilili.account.domain.EmailVerificationToken;
import com.nifilili.account.repository.EmailVerificationTokenRepository;
import com.nifilili.account.service.EmailVerificationService;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.core.email.EmailService;
import com.nifilili.core.exception.InvalidTokenException;
import com.nifilili.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final long VERIFICATION_TOKEN_EXPIRY_HOURS = 24;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void createAndSendVerification(Long userId, String email) {
        // Invalidate any existing unused tokens
        tokenRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(userId)
                .ifPresent(existing -> {
                    existing.setUsed(true);
                    tokenRepository.save(existing);
                });

        String tokenValue = UUID.randomUUID().toString();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .userId(userId)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_EXPIRY_HOURS))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        tokenRepository.save(token);
        emailService.sendVerificationEmail(email, tokenValue);
        log.info("Sent verification email to userId={}", userId);
    }

    @Override
    @Transactional
    public void verifyEmail(String tokenValue) {
        EmailVerificationToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("Verification token not found"));

        if (!token.isUsable()) {
            throw new InvalidTokenException("Verification token is expired or already used");
        }

        // Mark token as used
        token.setUsed(true);
        tokenRepository.save(token);

        // Mark user email as verified
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + token.getUserId()));

        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified for userId={}", token.getUserId());
    }

    @Override
    @Transactional
    public void resendVerification(Long userId, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (user.isEmailVerified()) {
            log.debug("Email already verified for userId={}, skipping resend", userId);
            return;
        }

        createAndSendVerification(userId, email);
        log.info("Resent verification email to userId={}", userId);
    }
}
