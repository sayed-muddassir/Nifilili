package com.nifilili.order.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.order.RefundStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_refunds")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefundEntity extends BaseEntity {

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    // Bank details for refund processing
    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String bankAccountName;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String branch;

    private String refundReference;

    private LocalDateTime processedAt;
}
