package com.nifilili.config.domain;

import com.nifilili.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "section_fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SectionField extends BaseEntity {

    private Long sectionId;
    private String name;
    private String label;
    private String type;
    private boolean required;
    private boolean allowMultiple;
}
