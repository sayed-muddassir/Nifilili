package com.nifilili.business.service.impl;

import com.nifilili.business.domain.SectionDefinition;
import com.nifilili.business.domain.SectionField;
import com.nifilili.business.dto.request.CreateSectionFieldRequest;
import com.nifilili.business.dto.request.CreateSectionRequest;
import com.nifilili.business.dto.response.SectionFieldResponse;
import com.nifilili.business.dto.response.SectionResponse;
import com.nifilili.business.mapper.SectionFieldMapper;
import com.nifilili.business.mapper.SectionMapper;
import com.nifilili.business.repository.SectionFieldRepository;
import com.nifilili.business.repository.SectionRepository;
import com.nifilili.business.service.SectionDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionDefinitionServiceImpl implements SectionDefinitionService {

    private final SectionRepository sectionRepository;
    private final SectionFieldRepository fieldRepository;
    private final SectionMapper sectionMapper;
    private final SectionFieldMapper fieldMapper;

    @Override
    public SectionResponse createSection(CreateSectionRequest request) {
        SectionDefinition saved = sectionRepository.save(sectionMapper.toEntity(request));
        return sectionMapper.toResponse(saved);
    }

    @Override
    public SectionFieldResponse addField(Long sectionId, CreateSectionFieldRequest request) {
        SectionField field = fieldMapper.toEntity(request);
        field.setSectionId(sectionId);
        return fieldMapper.toResponse(fieldRepository.save(field));
    }

    @Override
    public List<SectionResponse> getByVertical(Long verticalId) {
        return sectionRepository.findByVerticalId(verticalId)
                .stream()
                .map(sectionMapper::toResponse)
                .toList();
    }

    @Override
    public List<SectionFieldResponse> getFields(Long sectionId) {
        return fieldRepository.findBySectionId(sectionId)
                .stream()
                .map(fieldMapper::toResponse)
                .toList();
    }
}
