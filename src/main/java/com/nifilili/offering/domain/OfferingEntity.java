package com.nifilili.offering.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.offering.OfferingOwnerType;
import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.core.enums.offering.OfferingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "offerings",
        uniqueConstraints = {
                @UniqueConstraint(name = "offerings_sku_unique", columnNames = "sku")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferingEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private OfferingOwnerType ownerType; // business, freelancer

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private OfferingCategoryEntity category;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OfferingType type; // PRODUCT / SERVICE

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "sku")
    private String sku;

    @Column(name = "price", precision = 8, scale = 2)
    private BigDecimal price;

    @Column(name = "is_dynamic_pricing", nullable = false)
    private Boolean isDynamicPricing;

    @Column(name = "available_quantity")
    private Integer availableQuantity;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "images", columnDefinition = "jsonb")
    private List<String> images;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "is_b2b_enabled", nullable = false)
    private Boolean isB2bEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OfferingStatus status; // DRAFT, PUBLISHED, ARCHIVED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;
}

