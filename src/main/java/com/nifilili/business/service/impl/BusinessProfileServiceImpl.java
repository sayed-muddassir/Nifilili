package com.nifilili.business.service.impl;

import com.nifilili.business.domain.Business;
import com.nifilili.business.dto.request.UpdateBusinessProfileRequest;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.business.service.BusinessProfileService;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessProfileServiceImpl implements BusinessProfileService {


    private final BusinessRepository businessRepository;

    @Override
    public void updateProfile(Long businessId, UpdateBusinessProfileRequest request) {

        Business business = loadOwnedBusiness(businessId);

        ensureEditable(business);

        business.setBusinessSummary(request.getBusinessSummary());
        business.setContacts(request.getContacts());
        business.setBusinessHours(request.getBusinessHours());
        business.setLatitude(request.getLatitude());
        business.setLongitude(request.getLongitude());

        businessRepository.save(business);
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
