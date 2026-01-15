package com.nifilili.kyc.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.business.DocumentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Entity
@Table(name = "business_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessDocument extends BaseEntity {

    private Long businessId;
    private Long documentDefinitionId;
    private String fileUrl;
    private String fileName;
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;
    private String rejectionReason;
    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected Instant createdAt;
    @LastModifiedDate
    @Column(nullable = false)
    protected Instant updatedAt;
}
