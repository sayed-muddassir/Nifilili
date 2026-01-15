package com.nifilili.offering.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record PublicVariantResponse(
        Long id,
        String sku,
        BigDecimal price,
        Map<String, String> attributes
) {}

