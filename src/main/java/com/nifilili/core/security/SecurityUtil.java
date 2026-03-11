package com.nifilili.core.security;

import com.nifilili.auth.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public final class SecurityUtil {

    private SecurityUtil() {
    }

    /**
     * Extracts the authenticated user's ID from the security context.
     *
     * @return user ID
     * @throws IllegalStateException if user not authenticated or principal invalid
     */
    public static Long getCurrentUserId() {
        return getCurrentPrincipal().getUserId();
    }

    /**
     * Returns whether the current authenticated user has a verified email.
     *
     * @return true if email is verified
     * @throws IllegalStateException if user not authenticated
     */
    public static boolean isEmailVerified() {
        return getCurrentPrincipal().isEmailVerified();
    }

    /**
     * Extracts the full {@link UserPrincipal} from the security context.
     *
     * @return the authenticated user principal
     * @throws IllegalStateException if user not authenticated or principal invalid
     */
    public static UserPrincipal getCurrentPrincipal() {
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

        return userPrincipal;
    }
}
