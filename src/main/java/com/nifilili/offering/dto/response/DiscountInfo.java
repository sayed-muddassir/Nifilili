package com.nifilili.offering.dto.response;

import com.nifilili.core.enums.util.DiscountType;

import java.math.BigDecimal;

public record DiscountInfo(
        DiscountType type,
        BigDecimal value
) {}

