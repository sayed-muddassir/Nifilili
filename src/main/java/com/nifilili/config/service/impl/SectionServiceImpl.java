package com.nifilili.config.service.impl;

import com.nifilili.config.domain.Section;
import com.nifilili.config.domain.SectionField;
import com.nifilili.config.dto.request.CreateSectionFieldRequest;
import com.nifilili.config.dto.request.CreateSectionRequest;
import com.nifilili.config.dto.response.SectionFieldResponse;
import com.nifilili.config.dto.response.SectionResponse;
import com.nifilili.config.mapper.SectionFieldMapper;
import com.nifilili.config.mapper.SectionMapper;
import com.nifilili.config.repository.SectionFieldRepository;
import com.nifilili.config.repository.SectionRepository;
import com.nifilili.config.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final SectionFieldRepository fieldRepository;
    private final SectionMapper sectionMapper;
    private final SectionFieldMapper fieldMapper;

    @Override
    public SectionResponse createSection(CreateSectionRequest request) {
        Section saved = sectionRepository.save(sectionMapper.toEntity(request));
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
