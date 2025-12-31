package com.nifilili.config.domain;

import com.nifilili.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "document_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDefinition extends BaseEntity {

    private Long verticalId;

    private String name;
    private String label;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> allowedExtensions;

    private Integer maxFileSize;
    private boolean required;
}
