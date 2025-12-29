package com.nifilili.business.repository;

import com.nifilili.business.domain.BusinessSectionData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessSectionDataRepository extends JpaRepository<BusinessSectionData, Long> {
    List<BusinessSectionData> findByBusinessId(Long businessId);
}
