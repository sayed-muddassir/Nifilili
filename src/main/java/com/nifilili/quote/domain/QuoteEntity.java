package com.nifilili.quote.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.quote.QuoteStatus;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quotes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteEntity extends BaseEntity {

    @Column(nullable = false)
    private Long requestId;

    private Long parentQuoteId;

    @Column(nullable = false, unique = true)
    private String quoteNumber;

    @Column(nullable = false, columnDefinition = "text")
    private String serviceDetails;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String currency;

    private Integer estimatedDurationDays;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> attachments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status;

    private LocalDateTime sentAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime rejectedAt;

    @Column(columnDefinition = "text")
    private String rejectionReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private Long updatedBy;
}
