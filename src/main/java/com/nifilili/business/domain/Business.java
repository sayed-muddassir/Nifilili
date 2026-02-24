package com.nifilili.business.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.business.BusinessSource;
import com.nifilili.core.enums.business.BusinessStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "business_master")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Business extends BaseEntity {

    // The authenticated user who created or currently owns this business profile.
    private Long ownerUserId;
    // Populated when an admin-seeded listing is claimed by a user.
    private Long claimedByUserId;

    private Long verticalId;
    private String name;
    private String legalName;

    private Long municipalityId;
    private Integer wardNumber;
    private String toleName;
    @Column(name = "address_field_1")
    private String addressField1;
    @Column(name = "address_field_2")
    private String addressField2;
    private String postalCode;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> contacts;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> businessHours;

    private String website;

    @Enumerated(EnumType.STRING)
    private BusinessStatus status;

    @Enumerated(EnumType.STRING)
    private BusinessSource source;

    private boolean isClaimed;

    private BigDecimal averageRating;
    private Integer reviewCount;

    @Column(columnDefinition = "text")
    private String businessSummary;

    private Date registrationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
