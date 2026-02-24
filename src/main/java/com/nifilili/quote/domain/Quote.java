package com.nifilili.quote.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.quote.QuoteStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quotes")
@Getter
@Setter
public class Quote extends BaseEntity {

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    /** Self reference for revision chain */
    @Column(name = "parent_quote_id")
    private Long parentQuoteId;

    @Column(name = "quote_number", nullable = false, unique = true)
    private String quoteNumber;

    @Column(name = "service_details", columnDefinition = "TEXT", nullable = false)
    private String serviceDetails;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String currency;

    @Column(name = "estimated_duration_days")
    private Integer estimatedDurationDays;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> attachments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;
}
