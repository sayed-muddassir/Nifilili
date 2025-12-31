package com.nifilili.business.domain;

import com.nifilili.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    private Object attributeValue;
}
