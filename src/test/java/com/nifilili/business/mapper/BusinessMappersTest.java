package com.nifilili.business.mapper;

import com.nifilili.business.domain.*;
import com.nifilili.business.dto.request.*;
import com.nifilili.business.dto.response.*;
import com.nifilili.core.enums.business.BusinessAttributeFieldType;
import com.nifilili.core.enums.business.BusinessSectionFieldType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BusinessMappersTest {

    private final VerticalMapper verticalMapper = Mappers.getMapper(VerticalMapper.class);
    private final CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);
    private final SectionMapper sectionMapper = Mappers.getMapper(SectionMapper.class);
    private final SectionFieldMapper sectionFieldMapper = Mappers.getMapper(SectionFieldMapper.class);
    private final AttributeDefinitionMapper attributeDefinitionMapper = Mappers.getMapper(AttributeDefinitionMapper.class);
    private final DocumentDefinitionMapper documentDefinitionMapper = Mappers.getMapper(DocumentDefinitionMapper.class);
    private final BusinessAttributeMapper businessAttributeMapper = Mappers.getMapper(BusinessAttributeMapper.class);

    @Test
    void allMappers_WhenEntityAndResponseMappingsAreUsed_ShouldMapExpectedFields() {
        CreateVerticalRequest verticalRequest = new CreateVerticalRequest();
        verticalRequest.setName("Food");
        verticalRequest.setSlug("food");
        verticalRequest.setActive(true);

        VerticalDefinition vertical = verticalMapper.toEntity(verticalRequest);
        assertEquals("Food", vertical.getName());
        assertTrue(vertical.isActive());

        VerticalResponse verticalResponse = verticalMapper.toResponse(vertical);
        assertEquals("Food", verticalResponse.getName());

        CreateCategoryRequest categoryRequest = new CreateCategoryRequest();
        categoryRequest.setName("Cafe");
        categoryRequest.setBusinessVerticalId(10L);
        CategoryDefinition category = categoryMapper.toEntity(categoryRequest);
        assertEquals("Cafe", category.getName());

        CategoryResponse categoryResponse = categoryMapper.toResponse(category);
        assertEquals(10L, categoryResponse.getBusinessVerticalId());

        CreateSectionRequest sectionRequest = new CreateSectionRequest();
        sectionRequest.setVerticalId(1L);
        sectionRequest.setName("General");
        sectionRequest.setPromptText("Prompt");
        SectionDefinition section = sectionMapper.toEntity(sectionRequest);
        assertEquals("General", section.getName());

        SectionResponse sectionResponse = sectionMapper.toResponse(section);
        assertEquals("General", sectionResponse.getName());

        CreateSectionFieldRequest fieldRequest = new CreateSectionFieldRequest();
        fieldRequest.setName("phone");
        fieldRequest.setType(BusinessSectionFieldType.valueOf("TEXT"));
        fieldRequest.setOptions(List.of("A", "B"));

        SectionField field = sectionFieldMapper.toEntity(fieldRequest);
        // sectionId is intentionally ignored in mapper and assigned by service layer.
        assertNull(field.getSectionId());
        assertEquals("phone", field.getName());

        SectionFieldResponse fieldResponse = sectionFieldMapper.toResponse(field);
        assertEquals("phone", fieldResponse.getName());

        CreateAttributeDefinitionRequest attributeRequest = new CreateAttributeDefinitionRequest();
        attributeRequest.setVerticalId(8L);
        attributeRequest.setName("has_wifi");
        attributeRequest.setType(BusinessAttributeFieldType.valueOf("BOOLEAN"));

        AttributeDefinition attribute = attributeDefinitionMapper.toEntity(attributeRequest);
        assertEquals(8L, attribute.getVerticalId());
        assertNotNull(attribute.getCreatedAt());

        AttributeDefinitionResponse attributeResponse = attributeDefinitionMapper.toResponse(attribute);
        assertEquals("has_wifi", attributeResponse.getName());

        CreateDocumentDefinitionRequest documentRequest = new CreateDocumentDefinitionRequest();
        documentRequest.setVerticalId(8L);
        documentRequest.setName("PAN");

        DocumentDefinition document = documentDefinitionMapper.toEntity(documentRequest);
        assertEquals("PAN", document.getName());
        assertNotNull(document.getUpdatedAt());

        DocumentDefinitionResponse documentResponse = documentDefinitionMapper.toResponse(document);
        assertEquals("PAN", documentResponse.getName());

        BusinessAttribute businessAttribute = new BusinessAttribute();
        businessAttribute.setAttributeId(12L);
        businessAttribute.setAttributeValue(Map.of("value", true));

        BusinessAttributeResponse response = businessAttributeMapper.toResponse(businessAttribute);
        assertEquals(12L, response.getAttributeId());
        assertEquals(Map.of("value", true), response.getAttributeValue());
        assertNull(response.getName());
    }
}
