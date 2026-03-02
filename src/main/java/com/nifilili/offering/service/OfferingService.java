package com.nifilili.offering.service;

import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.dto.response.OfferingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OfferingService {
    /**
     * Returns a paginated list of offerings owned by the authenticated user, optionally filtered by status.
     *
     * @param status   filter by offering status (nullable for all statuses)
     * @param pageable pagination parameters
     * @return page of offering responses
     */
    Page<OfferingResponse> listMyOfferings(OfferingStatus status, Pageable pageable);

    /**
     * Creates a new offering in DRAFT status, assigned to the given leaf category.
     *
     * @param offering           the offering entity with fields populated from the request
     * @param offeringCategoryId the leaf category to assign
     * @return the persisted offering entity
     * @throws IllegalArgumentException if the category is not a leaf
     */
    OfferingEntity create(OfferingEntity offering, Long offeringCategoryId);
    /**
     * Updates an existing offering. Archived offerings cannot be modified.
     *
     * @param id      the offering ID
     * @param updates entity with updated field values
     * @return the updated offering entity
     * @throws IllegalArgumentException if offering not found
     * @throws IllegalStateException    if offering is archived
     */
    OfferingEntity update(Long id, OfferingEntity updates);

    /**
     * Fetches an offering by ID, verifying it belongs to the authenticated owner.
     *
     * @param id the offering ID
     * @return the offering entity
     * @throws IllegalArgumentException if offering not found or not owned by the current user
     */
    OfferingEntity getMyOffering(Long id);

    /** Transitions offering to PUBLISHED status. */
    void publish(Long id);

    /** Transitions offering to ARCHIVED status. */
    void archive(Long id);

    /** Restores an archived offering back to DRAFT status. */
    void restore(Long id);

    /** Updates the available quantity for an offering. */
    void updateInventory(Long id, Integer quantity);

    /** Fetches an offering by ID, throwing if not found. */
    OfferingEntity get(Long id);
}

