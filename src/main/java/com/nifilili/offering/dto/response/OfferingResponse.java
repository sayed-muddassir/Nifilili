package com.nifilili.offering.dto.response;

import com.nifilili.core.enums.offering.OfferingStatus;

import java.math.BigDecimal;
import java.util.List;

public record OfferingResponse(
        Long id,
        String title,
        String description,
        OfferingStatus status,
        BigDecimal price,
        List<String> images
) {}

