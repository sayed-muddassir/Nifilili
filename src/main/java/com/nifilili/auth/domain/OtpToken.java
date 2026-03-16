package com.nifilili.auth.domain;

import com.nifilili.auth.domain.enums.IdentifierType;
import com.nifilili.auth.domain.enums.OtpPurpose;
import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Unified OTP token for all authentication scenarios: login, signup,
 * password reset, and identity verification.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "otp_tokens")
public class OtpToken extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String identifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "identifier_type", nullable = false, length = 10)
    private IdentifierType identifierType;

    @Column(nullable = false, length = 6)
    private String otp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OtpPurpose purpose;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;

    @Builder.Default
    @Column(nullable = false)
    private int attempts = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Override
    protected void prePersist() {
        super.prePersist();
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsable() {
        return !used && !isExpired();
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
