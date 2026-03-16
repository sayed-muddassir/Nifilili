package com.nifilili.account.events;

import java.util.Set;

/**
 * Published when an admin creates a new user account.
 * Consumed by the messaging module to deliver welcome notifications
 * (e.g., email/SMS with temporary password).
 *
 * @param userId   the newly created user's ID
 * @param name     the user's full name
 * @param email    the user's email (may be null for phone-only accounts)
 * @param phone    the user's phone number (may be null for email-only accounts)
 * @param roles    the assigned role names
 */
public record AdminUserCreatedEvent(
        Long userId,
        String name,
        String email,
        String phone,
        Set<String> roles
) {}
