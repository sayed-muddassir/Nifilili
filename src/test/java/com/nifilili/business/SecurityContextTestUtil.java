package com.nifilili.business;

import com.nifilili.auth.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * Test helper to populate SecurityContext for code paths that use SecurityUtil.
 */
public final class SecurityContextTestUtil {

    private SecurityContextTestUtil() {
    }

    public static void setAuthenticatedUser(long userId) {
        UserPrincipal principal = UserPrincipal.of(userId, "test@example.com", "pwd", List.of());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
