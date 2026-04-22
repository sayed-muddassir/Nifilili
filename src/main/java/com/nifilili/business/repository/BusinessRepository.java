package com.nifilili.business.repository;

import com.nifilili.business.domain.Business;
import com.nifilili.core.enums.business.BusinessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRepository extends JpaRepository<Business, Long> {
    Page<Business> findByStatus(BusinessStatus status, Pageable pageable);

    Page<Business> findByOwnerUserId(Long ownerUserId, Pageable pageable);
}
