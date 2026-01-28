package com.nifilili.order.dto.response.cart;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class CartItemResponse {
    private Long cartItemId;
    private Long offeringId;
    private Long variantId;
    private String title;
    private Map<String, Object> variantAttributes;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Boolean available;
    private BigDecimal subtotal;
}

