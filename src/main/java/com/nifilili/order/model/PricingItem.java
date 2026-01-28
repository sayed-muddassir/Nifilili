package com.nifilili.order.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PricingItem {

    private Long businessId;
    private Long offeringId;
    private String title;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
    private BigDecimal tax;
    private BigDecimal deliveryCharge;
    private BigDecimal discount;

}
