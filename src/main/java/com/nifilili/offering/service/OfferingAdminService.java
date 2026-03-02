package com.nifilili.offering.service;

import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.offering.domain.OfferingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OfferingAdminService {


    /**
     * Returns a paginated list of all offerings, optionally filtered by status.
     *
     * @param status   optional status filter (nullable for all statuses)
     * @param pageable pagination parameters
     * @return page of offering entities
     */
    Page<OfferingEntity> listAll(OfferingStatus status, Pageable pageable);

    /**
     * Admin force-changes the status of an offering (e.g., suspend by archiving).
     *
     * @param id        the offering ID
     * @param newStatus the target status
     * @return the updated offering entity
     * @throws IllegalArgumentException if the offering is not found
     */
    OfferingEntity changeStatus(Long id, OfferingStatus newStatus);
}
