package com.nifilili.kyc.domain;

import com.nifilili.common.entity.BaseEntity;
import com.nifilili.common.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Entity
@Table(name = "business_kyc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessKyc extends BaseEntity {

    private Long businessId;

    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus;

    @Column(columnDefinition = "text")
    private String adminMessage;

    private Integer submissionCount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    protected Instant updatedAt;
}
