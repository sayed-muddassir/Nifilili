package com.nifilili.offering.dto.request;

import com.nifilili.core.enums.offering.DiscountType;

import java.time.LocalDateTime;

public record CreateDiscountRequest(
        Long offeringId,
        Long variantId,
        DiscountType discountType,
        Long discountValue,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}

