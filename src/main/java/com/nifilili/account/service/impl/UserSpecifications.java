package com.nifilili.account.service.impl;

import com.nifilili.auth.domain.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * Composable JPA {@link Specification} builders for filtering {@link User} queries.
 * Used by the admin user-listing endpoint to support dynamic search and filtering.
 */
final class UserSpecifications {

    private UserSpecifications() {
        // utility class
    }

    /**
     * Filters users whose roles collection contains a role with the given name.
     */
    static Specification<User> hasRole(String roleName) {
        return (root, query, cb) -> cb.equal(root.join("roles").get("name"), roleName);
    }

    /**
     * Filters users by their enabled status.
     */
    static Specification<User> isEnabled(boolean enabled) {
        return (root, query, cb) -> cb.equal(root.get("enabled"), enabled);
    }

    /**
     * Filters users by their account-locked status.
     */
    static Specification<User> isAccountLocked(boolean locked) {
        return (root, query, cb) -> cb.equal(root.get("accountLocked"), locked);
    }

    /**
     * Performs a case-insensitive LIKE search across name, email, phone, and username.
     */
    static Specification<User> searchByKeyword(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("phone")), pattern),
                    cb.like(cb.lower(root.get("username")), pattern)
            );
        };
    }
}
