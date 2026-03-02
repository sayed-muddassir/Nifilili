package com.nifilili.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    @Test
    void of_ShouldCreateEnabledPrincipalWithProvidedValues() {
        UserPrincipal principal = UserPrincipal.of(
                10L,
                "john",
                "secret",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        assertEquals(10L, principal.getUserId());
        assertEquals("john", principal.getUsername());
        assertEquals("secret", principal.getPassword());
        assertTrue(principal.isEnabled());
        assertTrue(principal.isAccountNonExpired());
        assertTrue(principal.isAccountNonLocked());
        assertTrue(principal.isCredentialsNonExpired());
        assertEquals(1, principal.getAuthorities().size());
    }

    @Test
    void anonymous_ShouldCreateDisabledPrincipalWithNoAuthorities() {
        UserPrincipal principal = UserPrincipal.anonymous();

        assertNull(principal.getUserId());
        assertEquals("anonymous", principal.getUsername());
        assertEquals("", principal.getPassword());
        assertFalse(principal.isEnabled());
        assertTrue(principal.getAuthorities().isEmpty());
    }

    @Test
    void equalsAndHashCode_ShouldDependOnUserIdOnly() {
        UserPrincipal left = UserPrincipal.of(5L, "u1", "p1", true, List.of());
        UserPrincipal right = UserPrincipal.of(5L, "u2", "p2", true, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        UserPrincipal different = UserPrincipal.of(6L, "u1", "p1", true, List.of());

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertNotEquals(left, different);
        assertNotEquals(null, left);
    }
}

