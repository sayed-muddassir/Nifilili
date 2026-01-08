package com.nifilili.kyc.repository;

import com.nifilili.kyc.domain.BusinessKycHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessKycHistoryRepository
        extends JpaRepository<BusinessKycHistory, Long> {
}
