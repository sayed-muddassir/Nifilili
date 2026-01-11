package com.nifilili.business.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "business_attributes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAttribute extends BaseEntity {

    private Long businessId;

    private Long attributeId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attribute_value", columnDefinition = "jsonb")
    private Map<String, Object> attributeValue;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    protected Instant updatedAt;

}
