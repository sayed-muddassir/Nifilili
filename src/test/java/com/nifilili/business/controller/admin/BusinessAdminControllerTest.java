package com.nifilili.business.controller.admin;

import com.nifilili.business.dto.request.*;
import com.nifilili.business.dto.response.*;
import com.nifilili.business.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        when(verticalDefinitionService.create(verticalRequest)).thenReturn(verticalResponse);
        when(verticalDefinitionService.update(1L, verticalRequest)).thenReturn(verticalResponse);

        CreateCategoryRequest categoryRequest = new CreateCategoryRequest();
        CategoryResponse categoryResponse = new CategoryResponse();
        when(categoryDefinitionService.create(categoryRequest)).thenReturn(categoryResponse);
        when(categoryDefinitionService.update(2L, categoryRequest)).thenReturn(categoryResponse);

        CreateSectionRequest sectionRequest = new CreateSectionRequest();
        SectionResponse sectionResponse = SectionResponse.builder().build();
        when(sectionDefinitionService.createSection(sectionRequest)).thenReturn(sectionResponse);
        when(sectionDefinitionService.updateSection(3L, sectionRequest)).thenReturn(sectionResponse);

        CreateSectionFieldRequest fieldRequest = new CreateSectionFieldRequest();
        SectionFieldResponse fieldResponse = new SectionFieldResponse();
        when(sectionDefinitionService.addField(3L, fieldRequest)).thenReturn(fieldResponse);
        when(sectionDefinitionService.updateField(4L, fieldRequest)).thenReturn(fieldResponse);

        CreateAttributeDefinitionRequest attributeRequest = new CreateAttributeDefinitionRequest();
        AttributeDefinitionResponse attributeResponse = new AttributeDefinitionResponse();
        when(attributeDefinitionService.create(attributeRequest)).thenReturn(attributeResponse);
        when(attributeDefinitionService.update(5L, attributeRequest)).thenReturn(attributeResponse);

        CreateDocumentDefinitionRequest documentRequest = new CreateDocumentDefinitionRequest();
        DocumentDefinitionResponse documentResponse = new DocumentDefinitionResponse();
        when(documentDefinitionService.create(documentRequest)).thenReturn(documentResponse);
        when(documentDefinitionService.update(6L, documentRequest)).thenReturn(documentResponse);

        assertSame(verticalResponse, businessAdminController.createVertical(verticalRequest));
        assertSame(verticalResponse, businessAdminController.updateVertical(1L, verticalRequest));
        assertSame(categoryResponse, businessAdminController.createCategory(categoryRequest));
        assertSame(categoryResponse, businessAdminController.updateCategory(2L, categoryRequest));
        assertSame(sectionResponse, businessAdminController.createSection(sectionRequest));
        assertSame(sectionResponse, businessAdminController.updateSection(3L, sectionRequest));
        assertSame(fieldResponse, businessAdminController.createSectionField(3L, fieldRequest));
        assertSame(fieldResponse, businessAdminController.updateSectionField(4L, fieldRequest));
        assertSame(attributeResponse, businessAdminController.createAttribute(attributeRequest));
        assertSame(attributeResponse, businessAdminController.updateAttribute(5L, attributeRequest));
        assertSame(documentResponse, businessAdminController.createDocument(documentRequest));
        assertSame(documentResponse, businessAdminController.updateDocument(6L, documentRequest));
    }
}
