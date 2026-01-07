package com.nifilili.business.service;

import com.nifilili.business.dto.request.UpdateBusinessProfileRequest;

public interface BusinessProfileService {

    void updateProfile(Long businessId, UpdateBusinessProfileRequest request);
}
