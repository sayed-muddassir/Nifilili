package com.nifilili.business.repository;

import com.nifilili.business.domain.BusinessCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessCategoryRepository extends JpaRepository<BusinessCategory, Long> {
    void deleteByBusinessId(Long businessId);

    List<BusinessCategory> findByBusinessId(Long businessId);
}
