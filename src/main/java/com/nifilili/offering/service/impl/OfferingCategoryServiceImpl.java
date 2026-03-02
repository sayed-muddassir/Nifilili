package com.nifilili.offering.service.impl;

import com.nifilili.offering.domain.OfferingCategoryEntity;
import com.nifilili.offering.repository.OfferingCategoryRepository;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.offering.service.OfferingCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OfferingCategoryServiceImpl implements OfferingCategoryService {

    private final OfferingCategoryRepository categoryRepository;
    private final OfferingRepository offeringRepository;

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

    public OfferingCategoryEntity update(Long id, String name) {
        log.info("Updating category id={} name={}", id, name);
        OfferingCategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        category.setName(name);
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        log.info("Deleting category id={}", id);
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category not found");
        }
        if (categoryRepository.existsByParentCategoryId(id)) {
            throw new IllegalStateException("Cannot delete category with child categories");
        }
        if (offeringRepository.existsByCategoryId(id)) {
            throw new IllegalStateException("Cannot delete category with existing offerings");
        }
        categoryRepository.deleteById(id);
    }
}

