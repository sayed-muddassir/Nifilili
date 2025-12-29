//package com.nifilili.common.audit;
//
//import com.nifilili.auth.security.UserPrincipal;
//import org.springframework.data.domain.AuditorAware;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//import java.util.Optional;
//
//@Component
//public class AuditorAwareImpl implements AuditorAware<Long> {
//
//    @Override
//    public Optional<Long> getCurrentAuditor() {
//
//        Authentication authentication =
//                SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return Optional.empty();
//        }
//
//        // You MUST ensure your Principal exposes userId
//        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
//            return Optional.of(principal.getUserId());
//        }
//
//        return Optional.empty();
//    }
//}
//
//
