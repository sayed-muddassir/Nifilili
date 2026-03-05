package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class CartItemResponse {
    private Long cartItemId;
    private Long offeringId;
    private Long variantId;
    private Long businessId;
    private String title;
    private Map<String, Object> variantAttributes;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Boolean available;
    private BigDecimal subtotal;
}
