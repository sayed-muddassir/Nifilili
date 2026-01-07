package com.nifilili.business.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "business_category_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessCategory extends BaseEntity {

    private Long businessId;
    private Long categoryId;
}
