package com.nifilili.core.security;

import com.nifilili.auth.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Security context has no authenticated principal");
            throw new IllegalStateException("User not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal) || userPrincipal.getUserId() == null) {
            log.warn("Authenticated principal is not a valid UserPrincipal");
            throw new IllegalStateException("User not authenticated");
        }

        return userPrincipal.getUserId();
    }
}
