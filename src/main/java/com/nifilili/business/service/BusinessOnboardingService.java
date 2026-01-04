package com.nifilili.business.service;

import com.nifilili.business.dto.request.CreateBusinessRequest;
import com.nifilili.business.dto.request.SaveBusinessAttributeRequest;
import com.nifilili.business.dto.request.SaveSectionDataRequest;
import com.nifilili.business.dto.request.UpdateBusinessProfileRequest;

import java.util.List;

public interface BusinessOnboardingService {

    Long createBusiness(CreateBusinessRequest request);

    void updateProfile(Long businessId, UpdateBusinessProfileRequest request);

    void updateCategories(Long businessId, List<Long> categoryIds);

    void saveSectionData(Long businessId, Long sectionId, SaveSectionDataRequest request);

    void saveAttributeData(Long businessId, SaveBusinessAttributeRequest request);


}

