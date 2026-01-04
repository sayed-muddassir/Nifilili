package com.nifilili.kyc.domain;

import com.nifilili.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "business_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDocument extends BaseEntity {

    private Long businessId;
    private Long documentDefinitionId;

    private String fileUrl;
    private String fileName;

    private String status; // PENDING, APPROVED, REJECTED

    @Column(columnDefinition = "text")
    private String rejectionReason;
}
