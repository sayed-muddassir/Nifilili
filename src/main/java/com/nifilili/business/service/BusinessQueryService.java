package com.nifilili.business.service;

import com.nifilili.business.dto.response.BusinessResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BusinessQueryService {

    BusinessResponse getBusinessById(Long businessId);

    Page<BusinessResponse> getAllBusinesses(Pageable pageable);
}
