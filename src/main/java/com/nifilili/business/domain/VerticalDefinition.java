package com.nifilili.business.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "business_verticals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerticalDefinition extends BaseEntity {

    private String name;
    private String slug;
    private String description;
    private String iconUrl;
    private boolean isActive;
}
