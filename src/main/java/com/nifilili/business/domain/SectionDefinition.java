package com.nifilili.business.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SectionDefinition extends BaseEntity {

    private Long verticalId;
    private String name;
    private String label;
    private String prompt;
    private boolean required;
    private boolean allowMultiple;
    private boolean groupable;
}
