package com.nifilili.order.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "refund_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundItemEntity extends BaseEntity {

    @Column(nullable = false)
    private Long refundId;

    @Column(nullable = false)
    private Long returnRequestId;

    @Column(nullable = false)
    private Long orderItemId;

    @Column(nullable = false)
    private BigDecimal refundAmount;

    @Column(nullable = false)
    private String status;
}
