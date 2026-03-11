package com.nifilili.account.service.impl;

import com.nifilili.account.dto.response.ActiveSessionResponse;
import com.nifilili.account.dto.response.LoginHistoryResponse;
import com.nifilili.auth.domain.LoginHistory;
import com.nifilili.auth.domain.RefreshToken;
import com.nifilili.auth.repository.LoginHistoryRepository;
import com.nifilili.auth.repository.RefreshTokenRepository;
import com.nifilili.auth.service.TokenService;
import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountSessionServiceImplTest {

    @Mock private TokenService tokenService;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private LoginHistoryRepository loginHistoryRepository;

    @InjectMocks private AccountSessionServiceImpl service;

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    @Test
    void getActiveSessions_WhenSessionsExist_ShouldReturnSessionListWithCurrentFlag() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        LocalDateTime now = LocalDateTime.now();

        RefreshToken session1 = RefreshToken.builder()
                .userId(1L).token("token-1").deviceName("Chrome")
                .ipAddress("192.168.1.1").createdAt(now).build();
        session1.setId(100L);

        RefreshToken session2 = RefreshToken.builder()
                .userId(1L).token("token-2").deviceName("Firefox")
                .ipAddress("192.168.1.2").createdAt(now.minusDays(1)).build();
        session2.setId(200L);

        when(tokenService.getActiveSessions(1L)).thenReturn(List.of(session1, session2));

        List<ActiveSessionResponse> sessions = service.getActiveSessions("token-1");

        assertEquals(2, sessions.size());
        assertTrue(sessions.get(0).isCurrent());
        assertFalse(sessions.get(1).isCurrent());
        assertEquals("Chrome", sessions.get(0).getDeviceName());
        assertEquals("Firefox", sessions.get(1).getDeviceName());
    }

    @Test
    void getActiveSessions_WhenNoSessions_ShouldReturnEmptyList() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        when(tokenService.getActiveSessions(1L)).thenReturn(List.of());

        List<ActiveSessionResponse> sessions = service.getActiveSessions("any-token");

        assertTrue(sessions.isEmpty());
    }

    @Test
    void revokeSession_WhenSessionBelongsToUser_ShouldRevokeAndSave() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        RefreshToken token = RefreshToken.builder()
                .userId(1L).token("session-token").revoked(false).build();
        token.setId(100L);

        when(refreshTokenRepository.findById(100L)).thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service.revokeSession(100L);

        assertTrue(token.isRevoked());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void revokeSession_WhenSessionNotFound_ShouldThrowResourceNotFoundException() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        when(refreshTokenRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.revokeSession(999L));
    }

    @Test
    void revokeSession_WhenSessionBelongsToDifferentUser_ShouldThrowResourceNotFoundException() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        RefreshToken token = RefreshToken.builder()
                .userId(2L).token("other-user-token").revoked(false).build();
        token.setId(100L);

        when(refreshTokenRepository.findById(100L)).thenReturn(Optional.of(token));

        assertThrows(ResourceNotFoundException.class, () -> service.revokeSession(100L));
    }

    @Test
    void getLoginHistory_WhenHistoryExists_ShouldReturnPaginatedResults() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime now = LocalDateTime.now();

        LoginHistory entry = LoginHistory.builder()
                .userId(1L).ipAddress("192.168.1.1").userAgent("Mozilla/5.0")
                .deviceName("Chrome").loggedInAt(now).build();

        Page<LoginHistory> page = new PageImpl<>(List.of(entry), pageable, 1);
        when(loginHistoryRepository.findByUserIdOrderByLoggedInAtDesc(1L, pageable)).thenReturn(page);

        Page<LoginHistoryResponse> result = service.getLoginHistory(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("192.168.1.1", result.getContent().get(0).getIpAddress());
        assertEquals("Chrome", result.getContent().get(0).getDeviceName());
    }

    @Test
    void getLoginHistory_WhenNoHistory_ShouldReturnEmptyPage() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        Pageable pageable = PageRequest.of(0, 10);

        Page<LoginHistory> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(loginHistoryRepository.findByUserIdOrderByLoggedInAtDesc(1L, pageable)).thenReturn(emptyPage);

        Page<LoginHistoryResponse> result = service.getLoginHistory(pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}
