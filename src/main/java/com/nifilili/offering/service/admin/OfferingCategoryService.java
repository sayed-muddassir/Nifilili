package com.nifilili.offering.service.admin;

import com.nifilili.offering.domain.OfferingCategoryEntity;
import com.nifilili.offering.repository.OfferingCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferingCategoryService {

    private final OfferingCategoryRepository categoryRepository;

    public OfferingCategoryEntity create(String name, Long parentCategoryId) {
        OfferingCategoryEntity category = new OfferingCategoryEntity();
        category.setName(name);

        if (parentCategoryId != null) {
            OfferingCategoryEntity parent = categoryRepository.findById(parentCategoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
            category.setParentCategory(parent);
        }

        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<OfferingCategoryEntity> getTree() {
        return categoryRepository.findByParentCategoryIsNull();
    }

    public OfferingCategoryEntity validateLeafCategory(Long categoryId) {
        boolean hasChildren = categoryRepository.existsByParentCategoryId(categoryId);
        if (hasChildren) {
            throw new IllegalStateException("Offering must be assigned to a leaf category");
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }
}

