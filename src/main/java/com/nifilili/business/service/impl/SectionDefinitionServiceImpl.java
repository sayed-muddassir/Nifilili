package com.nifilili.business.service.impl;

import com.nifilili.business.domain.SectionDefinition;
import com.nifilili.business.domain.SectionField;
import com.nifilili.business.domain.CategoryDefinition;
import com.nifilili.business.dto.request.CreateSectionFieldRequest;
import com.nifilili.business.dto.request.CreateSectionRequest;
import com.nifilili.business.dto.response.SectionFieldResponse;
import com.nifilili.business.dto.response.SectionResponse;
import com.nifilili.business.mapper.SectionFieldMapper;
import com.nifilili.business.mapper.SectionMapper;
import com.nifilili.business.repository.CategoryRepository;
import com.nifilili.business.repository.SectionFieldRepository;
import com.nifilili.business.repository.SectionRepository;
import com.nifilili.business.service.SectionDefinitionService;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionDefinitionServiceImpl implements SectionDefinitionService {

    private final SectionRepository sectionRepository;
    private final SectionFieldRepository fieldRepository;
    private final CategoryRepository categoryRepository;
    private final SectionMapper sectionMapper;
    private final SectionFieldMapper fieldMapper;

    @Override
    public SectionResponse createSection(CreateSectionRequest request) {
        validateSectionCategoryConsistency(request.getVerticalId(), request.getCategoryId());

        SectionDefinition sectionToPersist = sectionMapper.toEntity(request);

        SectionDefinition saved = sectionRepository.save(sectionToPersist);
        return sectionMapper.toResponse(saved);
    }

    @Override
    public SectionResponse updateSection(Long sectionId, CreateSectionRequest request) {
        validateSectionCategoryConsistency(request.getVerticalId(), request.getCategoryId());

        SectionDefinition existing = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        existing.setVerticalId(request.getVerticalId());
        existing.setCategoryId(request.getCategoryId());
        existing.setName(request.getName());
        existing.setLabel(request.getLabel());
        existing.setPrompt(request.getPromptText());
        existing.setRequired(request.isRequired());
        existing.setAllowMultiple(request.isAllowMultiple());
        existing.setGroupable(request.isGroupable());

        return sectionMapper.toResponse(sectionRepository.save(existing));
    }

    @Override
    public SectionFieldResponse addField(Long sectionId, CreateSectionFieldRequest request) {
        SectionField field = fieldMapper.toEntity(request);
        field.setSectionId(sectionId);
        return fieldMapper.toResponse(fieldRepository.save(field));
    }

    @Override
    public SectionFieldResponse updateField(Long fieldId, CreateSectionFieldRequest request) {
        SectionField existing = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Section field not found"));

        existing.setName(request.getName());
        existing.setLabel(request.getLabel());
        existing.setType(request.getType().name());
        existing.setOptions(request.getOptions());
        existing.setRequired(request.isRequired());
        existing.setAllowMultiple(request.isAllowMultiple());

        return fieldMapper.toResponse(fieldRepository.save(existing));
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

    private void validateSectionCategoryConsistency(Long verticalId, Long categoryId) {
        if (categoryId == null) {
            return;
        }

        CategoryDefinition category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getBusinessVerticalId().equals(verticalId)) {
            throw new InvalidBusinessStateException("Section category must belong to the same vertical");
        }
    }
}
