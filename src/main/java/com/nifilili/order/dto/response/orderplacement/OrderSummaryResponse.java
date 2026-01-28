package com.nifilili.order.dto.response.orderplacement;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderSummaryResponse {
    private Long orderId;
    private String orderNumber;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}

