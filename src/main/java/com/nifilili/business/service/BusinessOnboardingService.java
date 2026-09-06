package com.nifilili.business.service;

import com.nifilili.business.dto.request.CreateBusinessRequest;
import com.nifilili.business.dto.response.BulkCreateBusinessResponse;
import com.nifilili.business.dto.response.UserCreatedBusinessResponse;
import com.nifilili.core.enums.business.BusinessSource;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BusinessOnboardingService {

    Long createBusiness(CreateBusinessRequest request, BusinessSource businessSource);

    BulkCreateBusinessResponse bulkCreateBusinesses(List<CreateBusinessRequest> requests, BusinessSource businessSource);

    List<UserCreatedBusinessResponse> getLoggedInUserBusinesses(Pageable pageable);
}

