package com.nifilili.business.service.impl;

import com.nifilili.business.domain.Business;
import com.nifilili.business.dto.request.UpdateBusinessProfileRequest;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.business.service.BusinessProfileService;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
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
        if (request == null) {
            throw new IllegalArgumentException("UpdateBusinessProfileRequest must not be null");
        }

        Business business = loadOwnedBusiness(businessId);

        ensureEditable(business);

        // Only overwrite fields when request provides a non-null value. Otherwise, keep existing values.
        business.setBusinessSummary(java.util.Optional.ofNullable(request.getBusinessSummary()).orElse(business.getBusinessSummary()));
        business.setName(java.util.Optional.ofNullable(request.getName()).orElse(business.getName()));
        business.setLegalName(java.util.Optional.ofNullable(request.getLegalName()).orElse(business.getLegalName()));
        business.setMunicipalityId(java.util.Optional.ofNullable(request.getMunicipalityId()).orElse(business.getMunicipalityId()));
        business.setWardNumber(java.util.Optional.ofNullable(request.getWardNumber()).orElse(business.getWardNumber()));
        business.setToleName(java.util.Optional.ofNullable(request.getToleName()).orElse(business.getToleName()));
        business.setAddressField1(java.util.Optional.ofNullable(request.getAddressField1()).orElse(business.getAddressField1()));
        business.setAddressField2(java.util.Optional.ofNullable(request.getAddressField2()).orElse(business.getAddressField2()));
        business.setPostalCode(java.util.Optional.ofNullable(request.getPostalCode()).orElse(business.getPostalCode()));
        business.setWebsite(java.util.Optional.ofNullable(request.getWebsite()).orElse(business.getWebsite()));
        business.setProfileImageUrl(java.util.Optional.ofNullable(request.getProfileImageUrl()).orElse(business.getProfileImageUrl()));
        business.setBannerImageUrl(java.util.Optional.ofNullable(request.getBannerImageUrl()).orElse(business.getBannerImageUrl()));
        business.setContacts(java.util.Optional.ofNullable(request.getContacts()).orElse(business.getContacts()));
        business.setBusinessHours(java.util.Optional.ofNullable(request.getBusinessHours()).orElse(business.getBusinessHours()));
        business.setLatitude(java.util.Optional.ofNullable(request.getLatitude()).orElse(business.getLatitude()));
        business.setLongitude(java.util.Optional.ofNullable(request.getLongitude()).orElse(business.getLongitude()));

        try {
            businessRepository.save(business);
        } catch (org.springframework.dao.DataAccessException dae) {
            // Translate persistence errors into a clear runtime exception so GlobalExceptionHandler can map it.
            throw new RuntimeException("Failed to update business profile for id " + businessId + ": " + dae.getMessage(), dae);
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
}
