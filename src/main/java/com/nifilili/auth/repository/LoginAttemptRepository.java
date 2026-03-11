package com.nifilili.auth.repository;

import com.nifilili.auth.domain.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    /**
     * Counts consecutive failed login attempts for a username since the last successful login.
     */
    @Query("""
            SELECT COUNT(la) FROM LoginAttempt la
            WHERE la.username = :username AND la.success = false
            AND la.attemptedAt > COALESCE(
                (SELECT MAX(la2.attemptedAt) FROM LoginAttempt la2
                 WHERE la2.username = :username AND la2.success = true),
                CAST('1970-01-01' AS timestamp)
            )
            """)
    long countRecentFailedAttempts(@Param("username") String username);
}
