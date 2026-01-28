package com.nifilili.order.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PricingSummary {

    private BigDecimal subTotal;
    private BigDecimal totalTax;
    private BigDecimal deliveryCharge;
    private BigDecimal totalDiscount;
    private BigDecimal payableAmount;
}
