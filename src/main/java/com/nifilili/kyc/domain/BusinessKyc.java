package com.nifilili.kyc.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.KycStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected Instant createdAt;
    @LastModifiedDate
    @Column(nullable = false)
    protected Instant updatedAt;
    private Long businessId;
    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus;
    @Column(columnDefinition = "text")
    private String adminMessage;
    private Integer submissionCount;
}
