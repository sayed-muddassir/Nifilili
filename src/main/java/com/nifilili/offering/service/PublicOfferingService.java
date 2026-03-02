package com.nifilili.offering.service;

import com.nifilili.core.enums.offering.OfferingType;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.dto.response.PublicOfferingSummary;
import com.nifilili.offering.dto.response.PublicVariantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface PublicOfferingService {

    /**
     * Fetches a single published offering by ID.
     *
     * @param id the offering ID
     * @return the published offering entity
     * @throws IllegalArgumentException if the offering is not found or not published
     */
    OfferingEntity getPublishedOffering(Long id);

    /**
     * Fetches active variants for an offering, including their attribute key-value pairs.
     *
     * @param offeringId the parent offering ID
     * @return list of variant responses with attributes
     */
    List<PublicVariantResponse> getVariantsWithDetails(Long offeringId);

    /**
     * Searches published offerings with optional filters for category, type, and price range.
     * All filters are optional and combinable.
     *
     * @param categoryId filter by category (nullable)
     * @param type       filter by PRODUCT or SERVICE (nullable)
     * @param minPrice   minimum price inclusive (nullable)
     * @param maxPrice   maximum price inclusive (nullable)
     * @param pageable   pagination parameters
     * @return paginated search results
     */
    Page<PublicOfferingSummary> searchOfferings(Long categoryId,
                                                OfferingType type,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                Pageable pageable);

    /**
     * Returns a paginated list of published offerings belonging to a specific business/owner.
     *
     * @param ownerId  the business or individual owner ID
     * @param pageable pagination parameters
     * @return paginated summary of published offerings
     */
    Page<PublicOfferingSummary> listByOwner(Long ownerId, Pageable pageable);

    /**
     * Returns a paginated list of published offerings within a specific category.
     *
     * @param categoryId the category ID to filter by
     * @param pageable   pagination parameters
     * @return paginated summary of published offerings
     */
    Page<PublicOfferingSummary> listByCategory(Long categoryId, Pageable pageable);

    /**
     * Returns a paginated list of featured published offerings.
     *
     * @param pageable pagination parameters
     * @return paginated summary of featured offerings
     */
    Page<PublicOfferingSummary> listFeatured(Pageable pageable);
}

