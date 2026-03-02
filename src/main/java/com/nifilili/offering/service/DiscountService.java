package com.nifilili.offering.service;

import com.nifilili.offering.domain.OfferingDiscountEntity;
import com.nifilili.offering.dto.request.CreateDiscountRequest;
import com.nifilili.offering.dto.response.DiscountDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DiscountService {

    /**
     * Creates a time-bound discount for an offering or a specific variant.
     *
     * @param request contains offering ID, optional variant ID, discount type/value, and date range
     * @return the persisted discount entity
     * @throws IllegalArgumentException if the offering or variant is not found
     */
    OfferingDiscountEntity create(CreateDiscountRequest request) ;

    /**
     * Returns a paginated list of discounts for a given offering.
     *
     * @param offeringId the parent offering ID
     * @param pageable   pagination parameters
     * @return page of discount detail responses
     */
    Page<DiscountDetailResponse> listByOffering(Long offeringId, Pageable pageable);

    /**
     * Deactivates a discount by setting its status to INACTIVE.
     *
     * @param discountId the discount ID to deactivate
     * @throws IllegalArgumentException if the discount is not found
     */
    void deactivate(Long discountId);
}

