package com.nifilili.order.model;

import com.nifilili.core.enums.util.DiscountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

// TODO : Connect Coupon to DB table later

@Getter
@Builder
public class Coupon {

    private final Long businessId;
    private final String code;

    private final DiscountType discountType;
    private final BigDecimal discountValue;

    private final BigDecimal maxDiscount;
    private final BigDecimal minOrderAmount;

    private final LocalDate validFrom;
    private final LocalDate validTo;

    private final boolean active;
}

