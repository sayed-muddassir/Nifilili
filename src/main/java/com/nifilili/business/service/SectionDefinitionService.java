package com.nifilili.business.service;

import com.nifilili.business.dto.request.CreateSectionFieldRequest;
import com.nifilili.business.dto.request.CreateSectionRequest;
import com.nifilili.business.dto.response.SectionFieldResponse;
import com.nifilili.business.dto.response.SectionResponse;

import java.util.List;

public interface SectionDefinitionService {

    SectionResponse createSection(CreateSectionRequest request);
    SectionResponse updateSection(Long sectionId, CreateSectionRequest request);

    SectionFieldResponse addField(Long sectionId, CreateSectionFieldRequest request);
    SectionFieldResponse updateField(Long fieldId, CreateSectionFieldRequest request);

    List<SectionResponse> getByVertical(Long verticalId);

    List<SectionFieldResponse> getFields(Long sectionId);

    void delete(Long sectionId);
    void deleteField(Long fieldId);
}
