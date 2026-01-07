package com.nifilili.business.service;

import com.nifilili.business.dto.request.SaveBusinessAttributeRequest;

public interface BusinessAttributeService {

    void saveAttributeData(Long businessId, SaveBusinessAttributeRequest request);
}
