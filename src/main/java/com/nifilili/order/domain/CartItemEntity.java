package com.nifilili.order.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Getter @Setter
public class CartItemEntity extends BaseEntity {

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @Column(name = "offering_id", nullable = false)
    private Long offeringId;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(nullable = false)
    private Integer quantity;

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

