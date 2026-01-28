package com.nifilili.order.dto.response.business;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BusinessOrderItemResponse {
    private Long orderItemId;
    private Long orderId;
    private String title;
    private Integer quantity;
    private BigDecimal subtotal;
    private String status;
}

