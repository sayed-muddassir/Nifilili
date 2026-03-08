package com.nifilili.quote.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.quote.QuoteRequestStatus;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "quote_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequestEntity extends BaseEntity {

    @Column(nullable = false)
    private Long offeringId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long businessId;

    @Column(nullable = false, unique = true)
    private String requestNumber;

    @Column(nullable = false, columnDefinition = "text")
    private String requirements;

    private String budgetRange;

    private LocalDate preferredTimeline;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> deliveryAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> attachments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteRequestStatus status;

    @Column(columnDefinition = "text")
    private String rejectionReason;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private Long updatedBy;
}
