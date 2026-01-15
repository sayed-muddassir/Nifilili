package com.nifilili.offering.dto.response;

import java.math.BigDecimal;

public record PublicOfferingSummary(
        Long id,
        String title,
        BigDecimal price,
        boolean featured
) {}

