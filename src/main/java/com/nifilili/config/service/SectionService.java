package com.nifilili.config.service;

import com.nifilili.config.dto.request.CreateSectionFieldRequest;
import com.nifilili.config.dto.request.CreateSectionRequest;
import com.nifilili.config.dto.response.SectionFieldResponse;
import com.nifilili.config.dto.response.SectionResponse;

import java.util.List;

public interface SectionService {

    SectionResponse createSection(CreateSectionRequest request);

    SectionFieldResponse addField(Long sectionId, CreateSectionFieldRequest request);

    List<SectionResponse> getByVertical(Long verticalId);

    List<SectionFieldResponse> getFields(Long sectionId);
}
