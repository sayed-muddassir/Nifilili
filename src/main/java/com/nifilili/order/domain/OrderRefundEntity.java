package com.nifilili.order.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_refunds")
@Getter @Setter
public class OrderRefundEntity extends BaseEntity{

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "refund_amount", nullable = false)
    private BigDecimal refundAmount;

    @Column(nullable = false)
    private String status;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "bank_account_name", nullable = false)
    private String bankAccountName;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String branch;

    @Column(name = "refund_reference")
    private String refundReference;

    private LocalDateTime processedAt;
}

