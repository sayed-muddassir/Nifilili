package com.nifilili.business.domain;

import com.nifilili.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "section_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SectionGroup extends BaseEntity {

    private Long businessId;
    private Long sectionId;
    private String name;
}
