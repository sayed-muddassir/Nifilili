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
@Table(name = "order_payments")
@Getter @Setter
public class OrderPaymentEntity extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "payment_type_id", nullable = false)
    private Long paymentTypeId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_details", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> paymentDetails;

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

