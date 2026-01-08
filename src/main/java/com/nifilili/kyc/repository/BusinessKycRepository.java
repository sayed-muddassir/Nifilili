package com.nifilili.kyc.repository;

import com.nifilili.core.enums.KycStatus;
import com.nifilili.kyc.domain.BusinessKyc;
import com.nifilili.kyc.repository.projection.AdminKycListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BusinessKycRepository
        extends JpaRepository<BusinessKyc, Long> {

    Optional<BusinessKyc> findByBusinessId(Long businessId);

    @Query("""
                select 
                    b.id as businessId,
                    b.name as businessName,
                    b.verticalId as verticalId,
                    k.kycStatus as kycStatus,
                    k.submissionCount as submissionCount
                from BusinessKyc k
                join Business b on b.id = k.businessId
                where k.kycStatus = :status
            """)
    Page<AdminKycListProjection> findByStatus(
            KycStatus status,
            Pageable pageable
    );
}
// TODO Add once we add the Audit fields
// k.updatedAt as updatedAt