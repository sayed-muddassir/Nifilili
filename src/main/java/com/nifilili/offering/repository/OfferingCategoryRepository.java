package com.nifilili.offering.repository;

import com.nifilili.offering.domain.OfferingCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferingCategoryRepository
        extends JpaRepository<OfferingCategoryEntity, Long> {

    // Root categories (parent is null)
    List<OfferingCategoryEntity> findByParentCategoryIsNull();

    // Subcategories of a parent
    List<OfferingCategoryEntity> findByParentCategoryId(Long parentCategoryId);

    // Check if category has children
    boolean existsByParentCategoryId(Long parentCategoryId);

    // Validate category existence
    boolean existsById(Long id);
}

