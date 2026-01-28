package com.nifilili.order.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "return_requests")
@Getter @Setter
public class ReturnRequestEntity extends BaseEntity {

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(nullable = false)
    private String reason;

    @Column(name = "reason_details", columnDefinition = "TEXT")
    private String reasonDetails;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> photos;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pickup_address", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> pickupAddress;

    @Column(nullable = false)
    private String status;

    @Column(name = "rma_number")
    private String rmaNumber;

    private LocalDateTime requestedAt;
    private LocalDateTime pickupScheduledAt;
    private LocalDateTime receivedAt;
    private LocalDateTime inspectedAt;
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

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

