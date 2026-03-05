package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RefundResponse {
    private Long refundId;
    private BigDecimal refundAmount;
    private String status;
}
