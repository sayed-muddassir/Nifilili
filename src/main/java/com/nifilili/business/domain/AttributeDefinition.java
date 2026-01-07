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
import java.util.List;

@Entity
@Table(name = "attribute_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDefinition extends BaseEntity {

    private Long verticalId;

    private String name;        // e.g. has_parking
    private String label;       // e.g. Has Parking

    private String type;        // Text, number, dropdown, checkbox, boolean

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> options;

    private boolean required;
    private boolean allowMultiple;

    @Column(columnDefinition = "text")
    private String prompt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    protected Instant updatedAt;
}
