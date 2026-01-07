package com.nifilili.business.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDefinition extends BaseEntity {

    private Long businessVerticalId;
    private Long parentCategoryId;

    private String name;
    private String slug;
    private String description;
    private String iconUrl;

    private boolean activeStatus;
}
