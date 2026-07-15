package com.nifilili.business.service.impl;

import com.nifilili.business.domain.Business;
import com.nifilili.business.domain.BusinessCategory;
import com.nifilili.business.domain.CategoryDefinition;
import com.nifilili.business.repository.BusinessCategoryRepository;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.business.repository.CategoryRepository;
import com.nifilili.business.service.BusinessCategoryService;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessCategoryServiceImpl implements BusinessCategoryService {

    private final BusinessRepository businessRepository;
    private final BusinessCategoryRepository businessCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void updateCategories(Long businessId, List<Long> categoryIds) {

        Business business = loadOwnedBusiness(businessId);
        ensureEditable(business);
        validateCategoriesForBusinessVertical(business, categoryIds);

        businessCategoryRepository.deleteByBusinessId(businessId);

        for (Long categoryId : categoryIds) {
            BusinessCategory mapping = new BusinessCategory(
                    businessId,
                    categoryId
            );
            businessCategoryRepository.save(mapping);
        }
    }

    private Business loadOwnedBusiness(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Business not found"));

        Long authenticatedUserId = SecurityUtil.getCurrentUserId();
        if (business.getOwnerUserId() == null || !business.getOwnerUserId().equals(authenticatedUserId)) {
            throw new InvalidBusinessStateException("Unauthorized access, user does not own this business");
        }
        return business;
    }

    private void ensureEditable(Business business) {
        if (business.getStatus() == BusinessStatus.DRAFT) {
            throw new InvalidBusinessStateException(
                    "Business cannot be edited in current state");
        }
    }

    private void validateCategoriesForBusinessVertical(Business business, List<Long> categoryIds) {
        Set<Long> encounteredCategoryIds = new HashSet<>();

        for (Long categoryId : categoryIds) {
            if (!encounteredCategoryIds.add(categoryId)) {
                throw new InvalidBusinessStateException("Duplicate category id: " + categoryId);
            }

            CategoryDefinition categoryDefinition = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));

            if (!categoryDefinition.isActiveStatus()) {
                throw new InvalidBusinessStateException("Inactive category cannot be assigned: " + categoryId);
            }

            if (!categoryDefinition.getBusinessVerticalId().equals(business.getVerticalId())) {
                throw new InvalidBusinessStateException("Category does not belong to business vertical: " + categoryId);
            }
        }
    }
}
