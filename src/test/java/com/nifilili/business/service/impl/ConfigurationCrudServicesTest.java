package com.nifilili.business.service.impl;

import com.nifilili.business.domain.*;
import com.nifilili.business.dto.request.*;
import com.nifilili.business.dto.response.*;
import com.nifilili.business.mapper.*;
import com.nifilili.business.repository.*;
import com.nifilili.core.enums.business.BusinessAttributeFieldType;
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
class ConfigurationCrudServicesTest {

    @Mock private VerticalRepository verticalRepository;
    @Mock private VerticalMapper verticalMapper;
    @InjectMocks private VerticalDefinitionServiceImpl verticalService;

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;
    @InjectMocks private CategoryDefinitionServiceImpl categoryService;

    @Mock private AttributeDefinitionRepository attributeRepository;
    @Mock private AttributeDefinitionMapper attributeMapper;
    @InjectMocks private AttributeDefinitionServiceImpl attributeService;

    @Mock private DocumentDefinitionRepository documentRepository;
    @Mock private DocumentDefinitionMapper documentMapper;
    @InjectMocks private DocumentDefinitionServiceImpl documentService;

    @Test
    void verticalService_WhenCreateUpdateAndGetActive_ShouldMapAllPaths() {
        CreateVerticalRequest request = new CreateVerticalRequest();
        request.setName("Food");
        request.setActive(true);

        VerticalDefinition mappedEntity = new VerticalDefinition();
        VerticalDefinition existingEntity = new VerticalDefinition();
        VerticalDefinition activeEntity = new VerticalDefinition();
        activeEntity.setActive(true);
        VerticalDefinition inactiveEntity = new VerticalDefinition();
        inactiveEntity.setActive(false);

        VerticalResponse response = new VerticalResponse();

        when(verticalMapper.toEntity(request)).thenReturn(mappedEntity);
        when(verticalRepository.save(mappedEntity)).thenReturn(mappedEntity);
        when(verticalMapper.toResponse(mappedEntity)).thenReturn(response);

        when(verticalRepository.findById(11L)).thenReturn(Optional.of(existingEntity));
        when(verticalRepository.save(existingEntity)).thenReturn(existingEntity);
        when(verticalMapper.toResponse(existingEntity)).thenReturn(response);

        when(verticalRepository.findById(12L)).thenReturn(Optional.empty());
        when(verticalRepository.findAll()).thenReturn(List.of(activeEntity, inactiveEntity));
        when(verticalMapper.toResponse(activeEntity)).thenReturn(response);

        assertSame(response, verticalService.create(request));
        assertSame(response, verticalService.update(11L, request));
        assertThrows(ResourceNotFoundException.class, () -> verticalService.update(12L, request));
        assertEquals(1, verticalService.getAllActive().size());
        assertEquals(2, verticalService.getAll().size());
    }

    @Test
    void categoryService_WhenCreateUpdateAndGetByVertical_ShouldMapAllPaths() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setBusinessVerticalId(100L);
        request.setName("Cafe");

        CategoryDefinition mappedEntity = new CategoryDefinition();
        CategoryDefinition existingEntity = new CategoryDefinition();
        CategoryDefinition verticalEntity = new CategoryDefinition();
        CategoryResponse response = new CategoryResponse();

        when(categoryMapper.toEntity(request)).thenReturn(mappedEntity);
        when(categoryRepository.save(mappedEntity)).thenReturn(mappedEntity);
        when(categoryMapper.toResponse(mappedEntity)).thenReturn(response);

        when(categoryRepository.findById(21L)).thenReturn(Optional.of(existingEntity));
        when(categoryRepository.save(existingEntity)).thenReturn(existingEntity);
        when(categoryMapper.toResponse(existingEntity)).thenReturn(response);
        when(categoryRepository.findById(22L)).thenReturn(Optional.empty());

        when(categoryRepository.findByBusinessVerticalId(100L)).thenReturn(List.of(verticalEntity));
        when(categoryMapper.toResponse(verticalEntity)).thenReturn(response);

        assertSame(response, categoryService.create(request));
        assertSame(response, categoryService.update(21L, request));
        assertThrows(ResourceNotFoundException.class, () -> categoryService.update(22L, request));
        assertEquals(1, categoryService.getByVertical(100L).size());
    }

    @Test
    void attributeService_WhenCreateUpdateGetAndMissingUpdate_ShouldCoverAllBranches() {
        CreateAttributeDefinitionRequest request = new CreateAttributeDefinitionRequest();
        request.setVerticalId(10L);
        request.setName("has_wifi");
        request.setType(BusinessAttributeFieldType.TEXT);

        AttributeDefinition mappedEntity = new AttributeDefinition();
        AttributeDefinition existingEntity = new AttributeDefinition();
        AttributeDefinition byVerticalEntity = new AttributeDefinition();
        AttributeDefinitionResponse response = new AttributeDefinitionResponse();

        when(attributeMapper.toEntity(request)).thenReturn(mappedEntity);
        when(attributeRepository.save(mappedEntity)).thenReturn(mappedEntity);
        when(attributeMapper.toResponse(mappedEntity)).thenReturn(response);

        when(attributeRepository.findById(31L)).thenReturn(Optional.of(existingEntity));
        when(attributeRepository.save(existingEntity)).thenReturn(existingEntity);
        when(attributeMapper.toResponse(existingEntity)).thenReturn(response);
        when(attributeRepository.findById(32L)).thenReturn(Optional.empty());

        when(attributeRepository.findByVerticalId(10L)).thenReturn(List.of(byVerticalEntity));
        when(attributeMapper.toResponse(byVerticalEntity)).thenReturn(response);

        assertSame(response, attributeService.create(request));
        assertSame(response, attributeService.update(31L, request));
        assertThrows(ResourceNotFoundException.class, () -> attributeService.update(32L, request));
        assertEquals(1, attributeService.getByVertical(10L).size());
    }

    @Test
    void documentService_WhenCreateUpdateGetAndMissingUpdate_ShouldCoverAllBranches() {
        CreateDocumentDefinitionRequest request = new CreateDocumentDefinitionRequest();
        request.setVerticalId(10L);
        request.setName("PAN");

        DocumentDefinition mappedEntity = new DocumentDefinition();
        DocumentDefinition existingEntity = new DocumentDefinition();
        DocumentDefinition byVerticalEntity = new DocumentDefinition();
        DocumentDefinitionResponse response = new DocumentDefinitionResponse();

        when(documentMapper.toEntity(request)).thenReturn(mappedEntity);
        when(documentRepository.save(mappedEntity)).thenReturn(mappedEntity);
        when(documentMapper.toResponse(mappedEntity)).thenReturn(response);

        when(documentRepository.findById(41L)).thenReturn(Optional.of(existingEntity));
        when(documentRepository.save(existingEntity)).thenReturn(existingEntity);
        when(documentMapper.toResponse(existingEntity)).thenReturn(response);
        when(documentRepository.findById(42L)).thenReturn(Optional.empty());

        when(documentRepository.findByVerticalId(10L)).thenReturn(List.of(byVerticalEntity));
        when(documentMapper.toResponse(byVerticalEntity)).thenReturn(response);

        assertSame(response, documentService.create(request));
        assertSame(response, documentService.update(41L, request));
        assertThrows(ResourceNotFoundException.class, () -> documentService.update(42L, request));
        assertEquals(1, documentService.getByVertical(10L).size());
    }
}
