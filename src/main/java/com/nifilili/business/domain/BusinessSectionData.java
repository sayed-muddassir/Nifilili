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

import java.util.Map;

@Entity
@Table(name = "business_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessSectionData extends BaseEntity {

    private Long businessId;
    private Long sectionId;
    private Long sectionGroupId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> fieldValues;
}
