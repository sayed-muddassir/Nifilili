package com.nifilili.auth.domain;

import com.nifilili.auth.domain.enums.ProviderType;
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
 * Tracks linked authentication methods for a user.
 * Supports multiple auth providers per user (email, phone, Google, Facebook).
 * Each user can have at most one entry per provider type.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_auth_providers")
public class UserAuthProvider extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 20)
    private ProviderType providerType;

    @Column(name = "provider_user_id")
    private String providerUserId;

    @Column(name = "linked_at", nullable = false)
    private LocalDateTime linkedAt;

    @Override
    protected void prePersist() {
        super.prePersist();
        if (this.linkedAt == null) {
            this.linkedAt = LocalDateTime.now();
        }
    }

    public void setId(Long id) {
        this.id = id;
    }
}
