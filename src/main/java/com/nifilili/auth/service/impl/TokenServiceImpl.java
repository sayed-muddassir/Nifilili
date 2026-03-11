package com.nifilili.auth.service.impl;

import com.nifilili.auth.domain.RefreshToken;
import com.nifilili.auth.repository.RefreshTokenRepository;
import com.nifilili.auth.service.TokenService;
import com.nifilili.core.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt-refresh-expiration-ms:2592000000}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId, String deviceName, String ipAddress, String userAgent) {
        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        RefreshToken saved = refreshTokenRepository.save(token);
        log.debug("Created refresh token for userId={} device='{}'", userId, deviceName);
        return saved;
    }

    @Override
    @Transactional
    public RefreshToken rotateRefreshToken(String rawToken) {
        RefreshToken existing = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (!existing.isUsable()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        // Revoke old token
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        // Issue new token with same device info
        RefreshToken newToken = createRefreshToken(
                existing.getUserId(),
                existing.getDeviceName(),
                existing.getIpAddress(),
                existing.getUserAgent()
        );

        log.debug("Rotated refresh token for userId={}", existing.getUserId());
        return newToken;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String rawToken) {
        RefreshToken token = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        token.setRevoked(true);
        refreshTokenRepository.save(token);
        log.info("Revoked refresh token for userId={} device='{}'", token.getUserId(), token.getDeviceName());
    }

    @Override
    @Transactional
    public void revokeAllRefreshTokens(Long userId) {
        int revoked = refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Revoked {} refresh tokens for userId={}", revoked, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> getActiveSessions(Long userId) {
        return refreshTokenRepository.findByUserIdAndRevokedFalse(userId).stream()
                .filter(rt -> !rt.isExpired())
                .collect(Collectors.toList());
    }
}
