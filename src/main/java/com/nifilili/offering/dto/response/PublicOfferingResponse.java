package com.nifilili.offering.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record PublicOfferingResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        BigDecimal discountedPrice,
        List<String> images,
        List<PublicVariantResponse> variants
) {}

