package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BusinessOrderItemResponse {
    private Long orderItemId;
    private Long orderId;
    private String orderNumber;
    private String title;
    private Integer quantity;
    private BigDecimal subtotal;
    private String status;
}
