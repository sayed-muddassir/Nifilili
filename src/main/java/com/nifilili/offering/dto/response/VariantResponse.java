package com.nifilili.offering.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record VariantResponse(
        Long id,
        String sku,
        BigDecimal price,
        Long availableQuantity,
        String status,
        List<String> images
) {}

