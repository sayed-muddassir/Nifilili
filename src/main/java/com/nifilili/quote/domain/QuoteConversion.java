package com.nifilili.quote.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "quote_conversions")
@Getter
@Setter
public class QuoteConversion extends BaseEntity {

    @Column(name = "quote_id", nullable = false)
    private Long quoteId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "converted_at", nullable = false)
    private LocalDateTime convertedAt;

    @Column(name = "converted_by", nullable = false)
    private Long convertedBy;
}
