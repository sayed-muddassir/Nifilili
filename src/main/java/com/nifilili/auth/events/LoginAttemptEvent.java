package com.nifilili.auth.events;

public record LoginAttemptEvent(
        Long userId,
        String username,
        String ipAddress,
        boolean success) {
}
