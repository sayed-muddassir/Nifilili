package com.nifilili.business.domain;

import com.nifilili.common.entity.BaseEntity;
import com.nifilili.common.enums.BusinessSource;
import com.nifilili.common.enums.BusinessStatus;
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

    private Long verticalId;
    private String name;

    private Long municipalityId;
    private Integer wardNumber;
    private String toleName;
    @Column(name = "address_field_1")
    private String addressField1;
    private String postalCode;

    private Long latitude;
    private Long longitude;

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
