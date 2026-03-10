package com.nifilili.business.api.impl;

import com.nifilili.business.api.BusinessQueryApi;
import com.nifilili.business.api.BusinessValidationApi;
import com.nifilili.business.api.model.BusinessMetaData;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class BusinessQueryImpl implements BusinessQueryApi {

    private final BusinessRepository businessRepository;

    @Override
    public BusinessMetaData getBusinessDetailsById(Long businessId) {
        return businessRepository.findById(businessId)
                .map(business -> {
                    BusinessMetaData metaData = new BusinessMetaData();
                    metaData.setBusinessId(business.getId());
                    metaData.setBusinessName(business.getName());
                    metaData.setVerticalId(business.getVerticalId());
                    metaData.setOwnerUserId(business.getOwnerUserId());
                    return metaData;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with id: " + businessId));
    }
}
