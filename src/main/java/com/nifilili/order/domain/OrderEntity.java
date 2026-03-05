package com.nifilili.order.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.order.OrderStatus;
import com.nifilili.core.enums.order.PaymentStatus;
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
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    // Delivery address snapshot
    @Column(nullable = false)
    private String receiverName;

    @Column(nullable = false)
    private String contactNumber;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Long municipalityId;

    @Column(nullable = false)
    private Integer wardNumber;

    @Column(nullable = false)
    private String toleName;

    @Column(name = "address_field_1", nullable = false)
    private String addressField1;

    @Column(nullable = false)
    private String postalCode;

    // Aggregated amounts
    @Column(nullable = false)
    private BigDecimal subtotalAmount;

    @Column(nullable = false)
    private BigDecimal deliveryCharge;

    @Column(nullable = false)
    private BigDecimal taxAmount;

    @Column(nullable = false)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private BigDecimal amountPaid;

    @Column(columnDefinition = "text")
    private String customerNotes;

    // B2B fields
    @Builder.Default
    @Column(name = "is_b2b_order", nullable = false)
    private Boolean isB2bOrder = false;

    private Long b2bQuoteId;

    private String source;

    private Long sourceQuoteId;

    private Long sourceRequestId;

    // Audit
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private Long updatedBy;
}
