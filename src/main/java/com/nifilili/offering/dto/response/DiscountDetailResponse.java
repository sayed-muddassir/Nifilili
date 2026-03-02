package com.nifilili.offering.dto.response;

import com.nifilili.core.enums.util.DiscountType;

import java.time.LocalDateTime;

public record DiscountDetailResponse(
        Long id,
        Long offeringId,
        Long variantId,
        DiscountType discountType,
        Long discountValue,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String status
) {}
