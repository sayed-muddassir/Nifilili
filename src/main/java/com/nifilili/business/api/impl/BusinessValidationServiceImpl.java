package com.nifilili.business.api.impl;

import com.nifilili.business.api.BusinessValidationApi;
import com.nifilili.business.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class BusinessValidationServiceImpl implements BusinessValidationApi {

    private final BusinessRepository businessRepository;

    @Override
    public boolean existsAndActive(Long businessId) {
        // query business table
        return businessRepository.existsById(businessId);
    }
}
