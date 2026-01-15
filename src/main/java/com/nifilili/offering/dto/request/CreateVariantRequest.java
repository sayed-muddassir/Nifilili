package com.nifilili.offering.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record CreateVariantRequest(
        String sku,
        BigDecimal price,
        Long availableQuantity,
        List<String> images
) {}

