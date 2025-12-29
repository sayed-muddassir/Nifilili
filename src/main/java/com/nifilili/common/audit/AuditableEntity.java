package com.nifilili.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

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

