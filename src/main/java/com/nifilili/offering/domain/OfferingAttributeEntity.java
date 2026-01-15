package com.nifilili.offering.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.util.AttributeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "offering_attributes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferingAttributeEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offering_category_id", nullable = false)
    private OfferingCategoryEntity category;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "attribute_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AttributeType attributeType; // text, number, dropdown

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "jsonb")
    private List<String> options;
}

