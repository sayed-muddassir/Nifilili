package com.nifilili.account.validation;

import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.core.exception.EmailNotVerifiedException;
import com.nifilili.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Guard utility checked by services before protected actions that require
 * a verified email (e.g. business creation, order placement, quote requests).
 *
 * <p>Usage: {@code emailVerifiedGuard.requireVerified(userId);}
 */
@Component
@RequiredArgsConstructor
public class EmailVerifiedGuard {

    private final UserRepository userRepository;

    /**
     * Throws {@link EmailNotVerifiedException} if the user's email is not verified.
     *
     * @param userId the user ID to check
     * @throws EmailNotVerifiedException if email not verified
     * @throws ResourceNotFoundException if user not found
     */
    public void requireVerified(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException(
                    "Email verification is required to perform this action. Please verify your email first.");
        }
    }
}
