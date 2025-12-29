package com.nifilili.common.entity;

import com.nifilili.common.audit.AuditableEntity;
import com.nifilili.common.id.IdGeneratorContext;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

@MappedSuperclass
public abstract class BaseEntity extends AuditableEntity {

    @Id
    @Column(nullable = false, updatable = false)
    protected Long id;

    @PrePersist
    protected void prePersist() {
        if (id == null) {
            id = IdGeneratorContext.generate();
        }
    }

    public Long getId() {
        return id;
    }
}
