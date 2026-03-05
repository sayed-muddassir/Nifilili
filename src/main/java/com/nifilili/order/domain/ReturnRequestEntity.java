package com.nifilili.order.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.order.ReturnStatus;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "return_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestEntity extends BaseEntity {

    @Column(nullable = false)
    private Long orderItemId;

    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "text")
    private String reasonDetails;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> photos;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> pickupAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status;

    private String rmaNumber;

    // Lifecycle timestamps — set when each phase is reached
    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime pickupScheduledAt;

    private LocalDateTime receivedAt;

    private LocalDateTime inspectedAt;

    private LocalDateTime rejectedAt;

    @Column(columnDefinition = "text")
    private String rejectionReason;

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
