package com.nifilili.config.domain;

import com.nifilili.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseEntity {

    private Long businessVerticalId;
    private Long parentCategoryId;

    private String name;
    private String slug;
    private String description;
    private String iconUrl;

    private boolean activeStatus;
}
