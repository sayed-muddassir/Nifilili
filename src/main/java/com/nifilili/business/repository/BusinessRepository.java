package com.nifilili.business.repository;

import com.nifilili.business.domain.Business;
import com.nifilili.core.enums.business.BusinessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BusinessRepository extends JpaRepository<Business, Long> {
    Page<Business> findByStatus(BusinessStatus status, Pageable pageable);

    Page<Business> findByOwnerUserId(Long ownerUserId, Pageable pageable);

    Page<Business> findByClaimedByUserIdAndStatus(Long claimedByUserId, BusinessStatus businessStatus, Pageable pageable);

    /**
     * Fetches top N businesses by vertical and municipality with ACTIVE status, sorted by average rating.
     *
     * @param verticalId the vertical ID to filter by
     * @param municipalityId the municipality ID to filter by
     * @param status the business status to filter by
     * @param limit the maximum number of businesses to return
     * @return list of businesses sorted by average rating descending
     */
    @Query(value = "SELECT * FROM business_master " +
                   "WHERE vertical_id = :verticalId " +
                   "AND municipality_id = :municipalityId " +
                   "ORDER BY average_rating DESC " +
                   "LIMIT :limit",
           nativeQuery = true)
    List<Business> findTopNByVerticalAndMunicipalityAndStatus(
        @Param("verticalId") Long verticalId,
        @Param("municipalityId") Long municipalityId,
        @Param("limit") int limit
    );
}
