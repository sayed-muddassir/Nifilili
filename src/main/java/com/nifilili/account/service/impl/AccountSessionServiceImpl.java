package com.nifilili.account.service.impl;

import com.nifilili.account.dto.response.ActiveSessionResponse;
import com.nifilili.account.dto.response.LoginHistoryResponse;
import com.nifilili.account.service.AccountSessionService;
import com.nifilili.auth.domain.RefreshToken;
import com.nifilili.auth.repository.LoginHistoryRepository;
import com.nifilili.auth.repository.RefreshTokenRepository;
import com.nifilili.auth.service.TokenService;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountSessionServiceImpl implements AccountSessionService {

    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ActiveSessionResponse> getActiveSessions(String currentRefreshToken) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<RefreshToken> sessions = tokenService.getActiveSessions(userId);

        return sessions.stream()
                .map(rt -> ActiveSessionResponse.builder()
                        .sessionId(rt.getId())
                        .deviceName(rt.getDeviceName())
                        .ipAddress(rt.getIpAddress())
                        .createdAt(rt.getCreatedAt())
                        .current(rt.getToken().equals(currentRefreshToken))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeSession(Long sessionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        RefreshToken token = refreshTokenRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (!token.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);
        log.info("Revoked session id={} for userId={}", sessionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoginHistoryResponse> getLoginHistory(Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        return loginHistoryRepository.findByUserIdOrderByLoggedInAtDesc(userId, pageable)
                .map(lh -> LoginHistoryResponse.builder()
                        .ipAddress(lh.getIpAddress())
                        .userAgent(lh.getUserAgent())
                        .deviceName(lh.getDeviceName())
                        .loggedInAt(lh.getLoggedInAt())
                        .build());
    }
}
