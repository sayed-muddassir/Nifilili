package com.nifilili.offering.dto.response;

import com.nifilili.core.enums.offering.DiscountType;

import java.math.BigDecimal;

public record DiscountInfo(
        DiscountType type,
        BigDecimal value
) {}

