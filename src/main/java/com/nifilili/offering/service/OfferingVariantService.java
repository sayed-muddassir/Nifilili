package com.nifilili.offering.service;

import com.nifilili.offering.domain.OfferingVariantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OfferingVariantService {

    /**
     * Creates a new variant linked to the specified offering.
     *
     * @param offeringId the parent offering ID
     * @param variant    the variant entity with fields populated from the request
     * @return the persisted variant entity
     * @throws IllegalArgumentException if the offering is not found
     */
    public OfferingVariantEntity create(Long offeringId, OfferingVariantEntity variant);

    /**
     * Updates an existing variant's SKU, price, quantity, and images.
     *
     * @param variantId the variant ID to update
     * @param updates   entity with updated field values
     * @return the updated variant entity
     * @throws IllegalArgumentException if variant not found
     */
    OfferingVariantEntity update(Long variantId, OfferingVariantEntity updates);

    /** Returns all variants belonging to the specified offering. */
    List<OfferingVariantEntity> list(Long offeringId);

    /** Returns a paginated list of variants for the specified offering. */
    Page<OfferingVariantEntity> listPaginated(Long offeringId, Pageable pageable);

    /** Updates the available quantity for a variant. */
    void updateInventory(Long variantId, Long quantity);

    /**
     * Deactivates a variant by setting its status to INACTIVE (soft delete).
     *
     * @param variantId the variant ID to deactivate
     * @throws IllegalArgumentException if the variant is not found
     */
    void deactivate(Long variantId);
}
