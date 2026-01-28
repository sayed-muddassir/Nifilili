package com.nifilili.order.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "refund_items")
@Getter @Setter
public class RefundItemEntity extends BaseEntity {

    @Column(name = "refund_id", nullable = false)
    private Long refundId;

    @Column(name = "return_request_id", nullable = false)
    private Long returnRequestId;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

//    @Column(name = "refund_amount", nullable = false)
//    private BigDecimal refundAmount; TODO Uncomment if needed in future

//    @Column(nullable = false)
//    private String status;
}
