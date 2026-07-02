package com.nifilili.business.service;

import com.nifilili.business.dto.response.BusinessResponse;
import com.nifilili.core.enums.business.BusinessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BusinessQueryService {

    BusinessResponse getBusinessById(Long businessId);

    Page<BusinessResponse> getAllBusinessesByStatus(Pageable pageable, BusinessStatus businessStatus);

    Page<BusinessResponse> getAllBusinessesByClaimUnderProgress(Pageable pageable, BusinessStatus businessStatus);

    List<BusinessResponse> getTopNByVerticalAndMunicipality(Long verticalId, Long municipalityId, int limit);

}
