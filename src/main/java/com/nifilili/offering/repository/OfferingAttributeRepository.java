package com.nifilili.offering.repository;

import com.nifilili.offering.domain.OfferingAttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferingAttributeRepository
        extends JpaRepository<OfferingAttributeEntity, Long> {

    // Attributes for category
    List<OfferingAttributeEntity> findByCategoryId(Long categoryId);

    // Check attribute usage safety (before delete)
    boolean existsByCategoryId(Long categoryId);
}

