package com.nifilili.offering.repository;

import com.nifilili.offering.domain.OfferingVariantAttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferingVariantAttributeRepository
        extends JpaRepository<OfferingVariantAttributeEntity, Long> {

    // Attributes of a variant
    List<OfferingVariantAttributeEntity> findByVariantId(Long variantId);

    // Delete attributes when variant is removed
    void deleteByVariantId(Long variantId);
}

