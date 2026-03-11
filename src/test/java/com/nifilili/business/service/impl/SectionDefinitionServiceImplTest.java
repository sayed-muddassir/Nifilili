package com.nifilili.business.service.impl;

import com.nifilili.business.domain.CategoryDefinition;
import com.nifilili.business.domain.SectionDefinition;
import com.nifilili.business.domain.SectionField;
import com.nifilili.business.dto.request.CreateSectionFieldRequest;
import com.nifilili.business.dto.request.CreateSectionRequest;
import com.nifilili.business.dto.response.SectionFieldResponse;
import com.nifilili.business.dto.response.SectionResponse;
import com.nifilili.business.mapper.SectionFieldMapper;
import com.nifilili.business.mapper.SectionMapper;
import com.nifilili.business.repository.CategoryRepository;
import com.nifilili.business.repository.SectionFieldRepository;
import com.nifilili.business.repository.SectionRepository;
import com.nifilili.core.enums.business.BusinessSectionFieldType;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SectionDefinitionServiceImplTest {

    @Mock private SectionRepository sectionRepository;
    @Mock private SectionFieldRepository sectionFieldRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SectionMapper sectionMapper;
    @Mock private SectionFieldMapper sectionFieldMapper;

    @InjectMocks
    private SectionDefinitionServiceImpl sectionService;

    @Test
    void createSection_WhenCategoryBelongsToSameVertical_ShouldSaveSectionAndSyncPromptFields() {
        CreateSectionRequest request = new CreateSectionRequest();
        request.setVerticalId(1L);
        request.setCategoryId(2L);
        request.setPromptText("Prompt");

        CategoryDefinition category = new CategoryDefinition();
        category.setBusinessVerticalId(1L);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        SectionDefinition sectionDefinition = new SectionDefinition();
        SectionDefinition savedSectionDefinition = new SectionDefinition();
        SectionResponse sectionResponse = SectionResponse.builder().build();

        when(sectionMapper.toEntity(request)).thenReturn(sectionDefinition);
        when(sectionRepository.save(sectionDefinition)).thenReturn(savedSectionDefinition);
        when(sectionMapper.toResponse(savedSectionDefinition)).thenReturn(sectionResponse);

        assertSame(sectionResponse, sectionService.createSection(request));
        assertEquals("Prompt", sectionDefinition.getPrompt());
    }

    @Test
    void createSection_WhenCategoryVerticalMismatchesOrMissing_ShouldThrowValidationErrors() {
        CreateSectionRequest mismatchRequest = new CreateSectionRequest();
        mismatchRequest.setVerticalId(10L);
        mismatchRequest.setCategoryId(2L);

        CategoryDefinition category = new CategoryDefinition();
        category.setBusinessVerticalId(1L);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        CreateSectionRequest missingCategoryRequest = new CreateSectionRequest();
        missingCategoryRequest.setVerticalId(1L);
        missingCategoryRequest.setCategoryId(3L);
        when(categoryRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(InvalidBusinessStateException.class, () -> sectionService.createSection(mismatchRequest));
        assertThrows(ResourceNotFoundException.class, () -> sectionService.createSection(missingCategoryRequest));
    }

    @Test
    void updateSectionAddFieldGetAndUpdateField_ShouldCoverOperationalMethods() {
        CreateSectionRequest sectionRequest = new CreateSectionRequest();
        sectionRequest.setVerticalId(1L);
        sectionRequest.setCategoryId(2L);
        sectionRequest.setName("General");
        sectionRequest.setLabel("General");
        sectionRequest.setPromptText("legacy");

        CategoryDefinition category = new CategoryDefinition();
        category.setBusinessVerticalId(1L);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        SectionDefinition existingSection = new SectionDefinition();
        when(sectionRepository.findById(10L)).thenReturn(Optional.of(existingSection));
        when(sectionRepository.save(existingSection)).thenReturn(existingSection);
        SectionResponse sectionResponse = SectionResponse.builder().build();
        when(sectionMapper.toResponse(existingSection)).thenReturn(sectionResponse);

        CreateSectionFieldRequest fieldRequest = new CreateSectionFieldRequest();
        fieldRequest.setName("phone");
        fieldRequest.setLabel("Phone");
        fieldRequest.setType(BusinessSectionFieldType.valueOf("TEXT"));

        SectionField mappedField = new SectionField();
        when(sectionFieldMapper.toEntity(fieldRequest)).thenReturn(mappedField);
        when(sectionFieldRepository.save(mappedField)).thenReturn(mappedField);

        SectionFieldResponse fieldResponse = new SectionFieldResponse();
        when(sectionFieldMapper.toResponse(mappedField)).thenReturn(fieldResponse);

        SectionField existingField = new SectionField();
        when(sectionFieldRepository.findById(20L)).thenReturn(Optional.of(existingField));
        when(sectionFieldRepository.save(existingField)).thenReturn(existingField);
        when(sectionFieldMapper.toResponse(existingField)).thenReturn(fieldResponse);

        when(sectionRepository.findByVerticalId(1L)).thenReturn(List.of(existingSection));
        when(sectionFieldRepository.findBySectionId(10L)).thenReturn(List.of(existingField));

        assertSame(sectionResponse, sectionService.updateSection(10L, sectionRequest));
        assertSame(fieldResponse, sectionService.addField(10L, fieldRequest));
        assertSame(fieldResponse, sectionService.updateField(20L, fieldRequest));
        assertEquals(1, sectionService.getByVertical(1L).size());
        assertEquals(1, sectionService.getFields(10L).size());
    }

    @Test
    void updateMethods_WhenEntityMissing_ShouldThrowNotFound() {
        when(sectionRepository.findById(99L)).thenReturn(Optional.empty());
        when(sectionFieldRepository.findById(88L)).thenReturn(Optional.empty());

        CreateSectionRequest sectionRequest = new CreateSectionRequest();
        sectionRequest.setVerticalId(1L);

        CreateSectionFieldRequest fieldRequest = new CreateSectionFieldRequest();

        assertThrows(ResourceNotFoundException.class, () -> sectionService.updateSection(99L, sectionRequest));
        assertThrows(ResourceNotFoundException.class, () -> sectionService.updateField(88L, fieldRequest));
    }
}
