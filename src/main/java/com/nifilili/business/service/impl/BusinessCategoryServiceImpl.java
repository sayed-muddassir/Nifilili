package com.nifilili.business.service.impl;

import com.nifilili.business.domain.Business;
import com.nifilili.business.domain.BusinessCategory;
import com.nifilili.business.repository.BusinessCategoryRepository;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.business.service.BusinessCategoryService;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessCategoryServiceImpl implements BusinessCategoryService {

    private final BusinessRepository businessRepository;
    private final BusinessCategoryRepository businessCategoryRepository;

    @Override
    public void updateCategories(Long businessId, List<Long> categoryIds) {

        Business business = loadOwnedBusiness(businessId);
        ensureEditable(business);

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

//        Long userId = SecurityUtil.getCurrentUserId();
//        if (!business.getOwnerId().equals(userId)) {
//            throw new InvalidBusinessStateException("Unauthorized access");
//        }
        return business;
    }

    private void ensureEditable(Business business) {
        if (business.getStatus() == BusinessStatus.PENDING
                || business.getStatus() == BusinessStatus.PUBLISHED) {
            throw new InvalidBusinessStateException(
                    "Business cannot be edited in current state");
        }
    }
}
