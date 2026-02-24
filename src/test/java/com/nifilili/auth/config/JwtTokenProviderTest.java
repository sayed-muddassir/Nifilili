package com.nifilili.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String VALID_SECRET = "abcdefghijklmnopqrstuvwxyz123456";

    @Test
    void validateSecret_WhenSecretMissing_ShouldThrow() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "");
        ReflectionTestUtils.setField(provider, "jwtExpirationDate", 1000L);

        assertThrows(IllegalStateException.class, provider::validateSecret);
    }

    @Test
    void validateSecret_WhenSecretTooShort_ShouldThrow() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "short-secret");
        ReflectionTestUtils.setField(provider, "jwtExpirationDate", 1000L);

        assertThrows(IllegalStateException.class, provider::validateSecret);
    }

    @Test
    void generateGetAndValidateToken_WhenTokenIsValid_ShouldSucceed() {
        JwtTokenProvider provider = createProvider(VALID_SECRET, 60_000L);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "john",
                "ignored",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = provider.generateToken(authentication);

        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals("john", provider.getUsername(token));
    }

    @Test
    void validateToken_WhenMalformedToken_ShouldReturnFalse() {
        JwtTokenProvider provider = createProvider(VALID_SECRET, 60_000L);

        assertFalse(provider.validateToken("this-is-not-a-jwt"));
    }

    @Test
    void validateToken_WhenTokenExpired_ShouldReturnFalse() {
        JwtTokenProvider provider = createProvider(VALID_SECRET, -1L);
        Authentication authentication = new UsernamePasswordAuthenticationToken("john", "ignored");
        String token = provider.generateToken(authentication);

        assertFalse(provider.validateToken(token));
    }

    private JwtTokenProvider createProvider(String secret, long expirationMillis) {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", secret);
        ReflectionTestUtils.setField(provider, "jwtExpirationDate", expirationMillis);
        provider.validateSecret();
        return provider;
    }
}

