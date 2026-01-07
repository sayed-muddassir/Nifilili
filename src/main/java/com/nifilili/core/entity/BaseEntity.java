package com.nifilili.core.entity;

import com.nifilili.core.audit.AuditableEntity;
import com.nifilili.core.id.IdGeneratorContext;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;

@Getter
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

}
