package com.nifilili.core.audit;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

//    @CreatedDate
//    @Column(nullable = false, updatable = false)
//    protected Instant createdAt;
//
//    @LastModifiedDate
//    @Column(nullable = false)
//    protected Instant updatedAt;
//
//    @CreatedBy
//    @Column(updatable = false)
//    protected Long createdBy;
//
//    @LastModifiedBy
//    protected Long updatedBy;
}

