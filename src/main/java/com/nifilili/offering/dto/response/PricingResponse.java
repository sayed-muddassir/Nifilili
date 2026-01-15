package com.nifilili.offering.dto.response;

import java.math.BigDecimal;

public record PricingResponse(
        BigDecimal basePrice,
        DiscountInfo discount,
        BigDecimal finalPrice
) {}

