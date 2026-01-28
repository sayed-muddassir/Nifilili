package com.nifilili.offering.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.util.DiscountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "offering_discounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferingDiscountEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offering_id", nullable = false)
    private OfferingEntity offering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private OfferingVariantEntity variant;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType; // Percentage / Fixed

    @Column(name = "discount_value", nullable = false)
    private Long discountValue;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "status", nullable = false)
    private String status;
}
