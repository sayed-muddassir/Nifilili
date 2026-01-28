package com.nifilili.order.dto.response.checkout;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckoutItemPreviewResponse {
    private Long businessId;
    private Long offeringId;
    private String title;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal deliveryCharge;
    private BigDecimal subtotal;
}

