package com.nifilili.offering.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "offering_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferingCategoryEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    // Self-referential relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private OfferingCategoryEntity parentCategory;
}
