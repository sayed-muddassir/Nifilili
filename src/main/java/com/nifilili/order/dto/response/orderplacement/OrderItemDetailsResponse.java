package com.nifilili.order.dto.response.orderplacement;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemDetailsResponse {
    private Long orderItemId;
    private Long businessId;
    private String title;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String status;
    private List<OrderTimelineResponse> timeline;
}

