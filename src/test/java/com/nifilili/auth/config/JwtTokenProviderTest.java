package com.nifilili.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String VALID_SECRET = "abcdefghijklmnopqrstuvwxyz123456";

    @Test
    void validateSecret_WhenSecretMissing_ShouldThrow() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "");
        ReflectionTestUtils.setField(provider, "accessExpirationMs", 1000L);

        assertThrows(IllegalStateException.class, provider::validateSecret);
    }

    @Test
    void validateSecret_WhenSecretTooShort_ShouldThrow() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "short-secret");
        ReflectionTestUtils.setField(provider, "accessExpirationMs", 1000L);

        assertThrows(IllegalStateException.class, provider::validateSecret);
    }

    @Test
    void generateAccessToken_WhenCalled_ShouldReturnValidTokenWithClaims() {
        JwtTokenProvider provider = createProvider(VALID_SECRET, 60_000L);

        String token = provider.generateAccessToken(1L, "john");

        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals("john", provider.getUsername(token));
        assertEquals(1L, provider.getUserId(token));
    }

    @Test
    void validateToken_WhenMalformedToken_ShouldReturnFalse() {
        JwtTokenProvider provider = createProvider(VALID_SECRET, 60_000L);

        assertFalse(provider.validateToken("this-is-not-a-jwt"));
    }

    @Test
    void validateToken_WhenTokenExpired_ShouldReturnFalse() {
        JwtTokenProvider provider = createProvider(VALID_SECRET, -1L);
        String token = provider.generateAccessToken(1L, "john");

        assertFalse(provider.validateToken(token));
    }

    @Test
    void getUserId_WhenTokenValid_ShouldReturnUserId() {
        JwtTokenProvider provider = createProvider(VALID_SECRET, 60_000L);
        String token = provider.generateAccessToken(42L, "jane");

        assertEquals(42L, provider.getUserId(token));
    }

    private JwtTokenProvider createProvider(String secret, long expirationMillis) {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", secret);
        ReflectionTestUtils.setField(provider, "accessExpirationMs", expirationMillis);
        provider.validateSecret();
        return provider;
    }
}
