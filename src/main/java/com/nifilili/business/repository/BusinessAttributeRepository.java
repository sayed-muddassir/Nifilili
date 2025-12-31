package com.nifilili.business.repository;

import com.nifilili.business.domain.BusinessAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessAttributeRepository
        extends JpaRepository<BusinessAttribute, Long> {

    List<BusinessAttribute> findByBusinessId(Long businessId);

    void deleteByBusinessIdAndAttributeId(Long businessId, Long attributeId);
}
