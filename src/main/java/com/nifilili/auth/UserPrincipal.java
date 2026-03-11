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
    private final boolean emailVerified;
    private final boolean accountLocked;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(
            Long userId,
            String username,
            String password,
            boolean enabled,
            boolean emailVerified,
            boolean accountLocked,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.emailVerified = emailVerified;
        this.accountLocked = accountLocked;
        this.authorities = authorities;
    }

    /* ---------- Factory Methods ---------- */

    public static UserPrincipal of(
            Long userId,
            String username,
            String password,
            boolean enabled,
            boolean emailVerified,
            boolean accountLocked,
            Collection<? extends GrantedAuthority> authorities
    ) {
        return new UserPrincipal(userId, username, password, enabled, emailVerified, accountLocked, authorities);
    }

    /** @deprecated Use the full factory that passes emailVerified and accountLocked from DB. */
    @Deprecated(since = "v1.2", forRemoval = true)
    public static UserPrincipal of(
            Long userId,
            String username,
            String password,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities
    ) {
        return new UserPrincipal(userId, username, password, enabled, true, false, authorities);
    }

    public static UserPrincipal anonymous() {
        return new UserPrincipal(
                null,
                "anonymous",
                "",
                false,
                false,
                false,
                Collections.emptyList()
        );
    }

    /* ---------- Custom Accessors ---------- */

    public Long getUserId() {
        return userId;
    }

    public boolean isEmailVerified() {
        return emailVerified;
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

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
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
