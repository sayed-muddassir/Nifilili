package com.nifilili.offering.dto.response;

import java.util.List;

public record PublicOfferingSearchResponse(
        List<PublicOfferingSummary> items,
        long total
) {}

