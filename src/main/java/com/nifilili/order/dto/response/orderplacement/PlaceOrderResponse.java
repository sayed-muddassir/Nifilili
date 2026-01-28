package com.nifilili.order.dto.response.orderplacement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResponse {
    private Long orderId;
    private String orderNumber;
    private String paymentStatus;
    private BigDecimal totalAmount;
}

