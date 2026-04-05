package com.nifilili.business.controller.admin;

import com.nifilili.business.dto.request.*;
import com.nifilili.business.dto.response.*;
import com.nifilili.business.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessAdminControllerTest {

    @Mock private VerticalDefinitionService verticalDefinitionService;
    @Mock private CategoryDefinitionService categoryDefinitionService;
    @Mock private SectionDefinitionService sectionDefinitionService;
    @Mock private AttributeDefinitionService attributeDefinitionService;
    @Mock private DocumentDefinitionService documentDefinitionService;

    @InjectMocks
    private BusinessAdminController businessAdminController;

    @Test
    void allAdminEndpoints_WhenServicesReturnResponses_ShouldDelegateAndReturnSameInstance() {
        CreateVerticalRequest verticalRequest = new CreateVerticalRequest();
        VerticalResponse verticalResponse = new VerticalResponse();
        List<VerticalResponse> allVerticalResponses = List.of(verticalResponse);

        when(verticalDefinitionService.getAll()).thenReturn(allVerticalResponses);
        when(verticalDefinitionService.create(verticalRequest)).thenReturn(verticalResponse);
        when(verticalDefinitionService.update(1L, verticalRequest)).thenReturn(verticalResponse);

        CreateCategoryRequest categoryRequest = new CreateCategoryRequest();
        CategoryResponse categoryResponse = new CategoryResponse();
        List<CategoryResponse> categoryResponses = List.of(categoryResponse);

        when(categoryDefinitionService.getByVertical(1L)).thenReturn(categoryResponses);
        when(categoryDefinitionService.create(categoryRequest)).thenReturn(categoryResponse);
        when(categoryDefinitionService.update(2L, categoryRequest)).thenReturn(categoryResponse);

        CreateSectionRequest sectionRequest = new CreateSectionRequest();
        SectionResponse sectionResponse = SectionResponse.builder().build();
        List<SectionResponse> sectionResponses = List.of(sectionResponse);

        when(sectionDefinitionService.getByVertical(1L)).thenReturn(sectionResponses);
        when(sectionDefinitionService.createSection(sectionRequest)).thenReturn(sectionResponse);
        when(sectionDefinitionService.updateSection(3L, sectionRequest)).thenReturn(sectionResponse);

        CreateSectionFieldRequest fieldRequest = new CreateSectionFieldRequest();
        SectionFieldResponse fieldResponse = new SectionFieldResponse();
        List<SectionFieldResponse> fieldResponses = List.of(fieldResponse);

        when(sectionDefinitionService.getFields(1L)).thenReturn(fieldResponses);
        when(sectionDefinitionService.addField(3L, fieldRequest)).thenReturn(fieldResponse);
        when(sectionDefinitionService.updateField(4L, fieldRequest)).thenReturn(fieldResponse);

        CreateAttributeDefinitionRequest attributeRequest = new CreateAttributeDefinitionRequest();
        AttributeDefinitionResponse attributeResponse = new AttributeDefinitionResponse();
        List<AttributeDefinitionResponse> attributeResponses = List.of(attributeResponse);

        when(attributeDefinitionService.getByVertical(1L)).thenReturn(attributeResponses);
        when(attributeDefinitionService.create(attributeRequest)).thenReturn(attributeResponse);
        when(attributeDefinitionService.update(5L, attributeRequest)).thenReturn(attributeResponse);

        CreateDocumentDefinitionRequest documentRequest = new CreateDocumentDefinitionRequest();
        DocumentDefinitionResponse documentResponse = new DocumentDefinitionResponse();
        List<DocumentDefinitionResponse> documentResponses = List.of(documentResponse);

        when(documentDefinitionService.getByVertical(1L)).thenReturn(documentResponses);
        when(documentDefinitionService.create(documentRequest)).thenReturn(documentResponse);
        when(documentDefinitionService.update(6L, documentRequest)).thenReturn(documentResponse);

        assertSame(allVerticalResponses, businessAdminController.getAllVerticals());
        assertSame(verticalResponse, businessAdminController.createVertical(verticalRequest));
        assertSame(verticalResponse, businessAdminController.updateVertical(1L, verticalRequest));
        assertSame(categoryResponses, businessAdminController.getCategoriesByVertical(1L));
        assertSame(categoryResponse, businessAdminController.createCategory(categoryRequest));
        assertSame(categoryResponse, businessAdminController.updateCategory(2L, categoryRequest));
        assertSame(sectionResponses, businessAdminController.getSectionsByVertical(1L));
        assertSame(sectionResponse, businessAdminController.createSection(sectionRequest));
        assertSame(sectionResponse, businessAdminController.updateSection(3L, sectionRequest));
        assertSame(fieldResponses, businessAdminController.getSectionFields(1L));
        assertSame(fieldResponse, businessAdminController.createSectionField(3L, fieldRequest));
        assertSame(fieldResponse, businessAdminController.updateSectionField(4L, fieldRequest));
        assertSame(attributeResponses, businessAdminController.getAttributesByVertical(1L));
        assertSame(attributeResponse, businessAdminController.createAttribute(attributeRequest));
        assertSame(attributeResponse, businessAdminController.updateAttribute(5L, attributeRequest));
        assertSame(documentResponses, businessAdminController.getDocumentByVertical(1L));
        assertSame(documentResponse, businessAdminController.createDocument(documentRequest));
        assertSame(documentResponse, businessAdminController.updateDocument(6L, documentRequest));
    }
}
