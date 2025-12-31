package com.nifilili.config.domain;

import com.nifilili.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "business_verticals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vertical extends BaseEntity {

    private String name;
    private String slug;
    private String description;
    private String iconUrl;
    private boolean isActive;
}
