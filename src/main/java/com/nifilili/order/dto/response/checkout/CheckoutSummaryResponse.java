package com.nifilili.order.dto.response.checkout;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckoutSummaryResponse {
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
    private BigDecimal deliveryCharge;
    private BigDecimal payableAmount;
}

