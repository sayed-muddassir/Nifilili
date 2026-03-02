package com.nifilili.offering.service;

import com.nifilili.offering.dto.response.PricingResponse;

import java.math.BigDecimal;

public interface PricingService {

    /**
     * Resolves the final price for an offering after applying any active offering-level discount.
     *
     * @param offeringId the offering ID
     * @return the discounted price, or the base price if no discount is active
     * @throws IllegalArgumentException if the offering is not found
     */
    BigDecimal resolveOfferingPrice(Long offeringId);

    /**
     * Returns full pricing breakdown for a variant: base price, discount info, and final price.
     *
     * @param variantId the variant ID
     * @return pricing response with base, discount, and final price
     * @throws IllegalArgumentException if the variant is not found
     */
    PricingResponse getVariantPriceDetails(Long variantId);
}

