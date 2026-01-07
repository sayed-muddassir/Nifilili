package com.nifilili.business.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "section_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessSectionGroup extends BaseEntity {

    private Long businessId;
    private Long sectionId;
    private String name;
}
