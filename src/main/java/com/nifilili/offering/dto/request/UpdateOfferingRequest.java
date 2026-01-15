package com.nifilili.offering.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record UpdateOfferingRequest(
        String title,
        String description,
        BigDecimal price,
        Integer availableQuantity,
        Boolean isFeatured,
        Boolean isB2bEnabled,
        List<String> images
) {}

