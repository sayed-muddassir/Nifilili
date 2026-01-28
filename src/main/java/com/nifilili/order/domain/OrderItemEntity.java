package com.nifilili.order.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItemEntity extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "offering_id", nullable = false)
    private Long offeringId;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String sku;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "variant_attributes", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> variantAttributes;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

    @Column(name = "delivery_charge", nullable = false)
    private BigDecimal deliveryCharge;

    @Column(name = "tax_amount", nullable = false)
    private BigDecimal taxAmount;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private String status;

    // TODO audit fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;
}
