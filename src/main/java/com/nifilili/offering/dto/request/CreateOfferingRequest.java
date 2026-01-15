package com.nifilili.offering.dto.request;

import com.nifilili.core.enums.offering.OfferingOwnerType;
import com.nifilili.core.enums.offering.OfferingType;

import java.math.BigDecimal;
import java.util.List;

public record CreateOfferingRequest(
        OfferingOwnerType ownerType,
        Long ownerId,
        Long categoryId,
        OfferingType type,
        String title,
        String description,
        String sku,
        BigDecimal price,
        Boolean isDynamicPricing,
        Integer availableQuantity,
        Boolean isFeatured,
        Boolean isB2bEnabled,
        List<String> images
) {}

