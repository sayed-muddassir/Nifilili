package com.nifilili.auth.service.impl;

import com.nifilili.auth.domain.RefreshToken;
import com.nifilili.auth.repository.RefreshTokenRepository;
import com.nifilili.core.exception.InvalidTokenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks private TokenServiceImpl tokenService;

    @Test
    void createRefreshToken_WhenCalled_ShouldPersistTokenWithDeviceInfo() {
        ReflectionTestUtils.setField(tokenService, "refreshExpirationMs", 2592000000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = tokenService.createRefreshToken(1L, "Chrome", "192.168.1.1", "Mozilla/5.0");

        assertNotNull(result.getToken());
        assertEquals(1L, result.getUserId());
        assertEquals("Chrome", result.getDeviceName());
        assertFalse(result.isRevoked());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertEquals("192.168.1.1", captor.getValue().getIpAddress());
    }

    @Test
    void rotateRefreshToken_WhenTokenValid_ShouldRevokeOldAndCreateNew() {
        ReflectionTestUtils.setField(tokenService, "refreshExpirationMs", 2592000000L);

        RefreshToken existing = RefreshToken.builder()
                .userId(1L)
                .token("old-token")
                .deviceName("Chrome")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken newToken = tokenService.rotateRefreshToken("old-token");

        assertTrue(existing.isRevoked());
        assertNotEquals("old-token", newToken.getToken());
        assertEquals(1L, newToken.getUserId());
    }

    @Test
    void rotateRefreshToken_WhenTokenExpired_ShouldThrowInvalidTokenException() {
        RefreshToken expired = RefreshToken.builder()
                .token("expired-token")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        assertThrows(InvalidTokenException.class, () -> tokenService.rotateRefreshToken("expired-token"));
    }

    @Test
    void revokeRefreshToken_WhenTokenExists_ShouldMarkRevoked() {
        RefreshToken token = RefreshToken.builder()
                .userId(1L)
                .token("some-token")
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("some-token")).thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        tokenService.revokeRefreshToken("some-token");

        assertTrue(token.isRevoked());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void revokeAllRefreshTokens_WhenCalled_ShouldDelegateToRepository() {
        when(refreshTokenRepository.revokeAllByUserId(1L)).thenReturn(3);

        tokenService.revokeAllRefreshTokens(1L);

        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }
}
