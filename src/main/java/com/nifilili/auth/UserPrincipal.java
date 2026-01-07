package com.nifilili.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(
            Long userId,
            String username,
            String password,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    /* ---------- Factory Methods ---------- */

    public static UserPrincipal of(
            Long userId,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities
    ) {
        return new UserPrincipal(
                userId,
                username,
                password,
                true,
                authorities
        );
    }

    public static UserPrincipal anonymous() {
        return new UserPrincipal(
                null,
                "anonymous",
                "",
                false,
                Collections.emptyList()
        );
    }

    /* ---------- Custom Accessors ---------- */

    public Long getUserId() {
        return userId;
    }

    /* ---------- UserDetails Contract ---------- */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Username is typically email or mobile number
     */
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // extend later if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // extend later if needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // extend later if needed
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /* ---------- Equality (CRITICAL for SecurityContext) ---------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPrincipal that)) return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
