package com.nifilili.kyc.repository;

import com.nifilili.kyc.domain.BusinessKyc;
import com.nifilili.kyc.repository.projection.AdminKycListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BusinessKycRepository
        extends JpaRepository<BusinessKyc, Long> {

    Optional<BusinessKyc> findByBusinessId(Long businessId);

    @Query(value = """
                SELECT
                    k.business_id      AS businessId,
                    b.name             AS businessName,
                    b.vertical_id      AS verticalId,
                    k.kyc_status       AS kycStatus,
                    k.submission_count AS submissionCount,
                    k.updated_at       AS updatedAt
                FROM business_kyc k
                JOIN business_master b ON b.id = k.business_id
                WHERE k.kyc_status = :status
            """, nativeQuery = true)
    Page<AdminKycListProjection> findByStatus(
            @Param("status") String status,
            Pageable pageable
    );
}