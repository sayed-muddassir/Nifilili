//package com.nifilili.common.security;
//
//import com.nifilili.auth.security.UserPrincipal;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//public final class SecurityUtil {
//
//    private SecurityUtil() {}
//
//    public static Long getCurrentUserId() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
//            throw new IllegalStateException("User not authenticated");
//        }
//
//        return principal.getUserId();
//    }
//}
