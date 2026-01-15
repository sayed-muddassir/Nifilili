package com.nifilili.offering.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "offering_variant_attributes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferingVariantAttributeEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offering_variant_id", nullable = false)
    private OfferingVariantEntity variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offering_attributes_id")
    private OfferingAttributeEntity offeringAttribute; // nullable for custom

    @Column(name = "attribute_name", nullable = false)
    private String attributeName;

    @Column(name = "attribute_value", nullable = false)
    private String attributeValue;
}
