package com.nifilili.config.domain;

import com.nifilili.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Section extends BaseEntity {

    private Long verticalId;
    private String name;
    private String label;
    private String prompt;
    private boolean required;
    private boolean allowMultiple;
    private boolean groupable;
}
