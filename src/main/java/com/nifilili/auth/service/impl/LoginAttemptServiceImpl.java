package com.nifilili.auth.service.impl;

import com.nifilili.auth.domain.LoginAttempt;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.LoginAttemptRepository;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.auth.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final LoginAttemptRepository loginAttemptRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void recordAttempt(Long userId, String username, String ipAddress, boolean success) {
        LoginAttempt attempt = LoginAttempt.builder()
                .userId(userId)
                .username(username)
                .ipAddress(ipAddress)
                .success(success)
                .attemptedAt(LocalDateTime.now())
                .build();

        log.info("Trying to save : {}", attempt.toString());
        log.info("Saved etity: {}", loginAttemptRepository.save(attempt));

        if (!success && userId != null) {
            long failedCount = loginAttemptRepository.countRecentFailedAttempts(username);
            if (failedCount >= MAX_FAILED_ATTEMPTS) {
                lockAccount(userId, username);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAccountLocked(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .map(User::isAccountLocked)
                .orElse(false);
    }

    private void lockAccount(Long userId, String username) {
        userRepository.findById(userId).ifPresent(user -> {
            if (!user.isAccountLocked()) {
                user.setAccountLocked(true);
                userRepository.save(user);
                log.warn("Account locked for userId={} username='{}' after {} failed attempts",
                        userId, username, MAX_FAILED_ATTEMPTS);
            }
        });
    }
}
