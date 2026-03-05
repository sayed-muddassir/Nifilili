package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PlaceOrderResponse {
    private Long orderId;
    private String orderNumber;
    private String paymentStatus;
    private BigDecimal totalAmount;
}
