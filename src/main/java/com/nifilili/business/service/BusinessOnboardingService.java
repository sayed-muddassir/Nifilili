package com.nifilili.business.service;

import com.nifilili.business.dto.request.CreateBusinessRequest;
import com.nifilili.business.dto.response.UserCreatedBusinessResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BusinessOnboardingService {

    Long createBusiness(CreateBusinessRequest request);

    List<UserCreatedBusinessResponse> getLoggedInUserBusinesses(Pageable pageable);
}

