package com.nifilili.business.service;

import com.nifilili.business.dto.request.SaveSectionDataRequest;

public interface BusinessSectionService {

    void saveSectionData(Long businessId, Long sectionId, SaveSectionDataRequest request);
}
