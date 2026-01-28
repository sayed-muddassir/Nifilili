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
@Table(name = "orders")
@Getter
@Setter
public class OrderEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_number", nullable = false)
    private String orderNumber;

    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @Column(nullable = false)
    private String email;

    @Column(name = "municipality_id", nullable = false)
    private Long municipalityId;

    @Column(name = "ward_number", nullable = false)
    private Integer wardNumber;

    @Column(name = "tole_name", nullable = false)
    private String toleName;

    @Column(name = "address_field_1", columnDefinition = "TEXT", nullable = false)
    private String addressField1;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "subtotal_amount", nullable = false)
    private BigDecimal subtotalAmount;

    @Column(name = "delivery_charge", nullable = false)
    private BigDecimal deliveryCharge;

    @Column(name = "tax_amount", nullable = false)
    private BigDecimal taxAmount;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String status;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;

    @Column(name = "amount_paid", nullable = false)
    private BigDecimal amountPaid;

    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    @Column(name = "is_b2b_order", nullable = false)
    private Boolean isB2bOrder;

    @Column(name = "b2b_quote_id")
    private Long b2bQuoteId;

    @Column(nullable = false)
    private String source;

    @Column(name = "source_quote_id")
    private Long sourceQuoteId;

    @Column(name = "source_request_id")
    private Long sourceRequestId;

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

