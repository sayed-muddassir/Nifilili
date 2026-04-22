package com.nifilili.business.controller.owner;

import com.nifilili.business.dto.request.*;
import com.nifilili.business.dto.response.*;
import com.nifilili.business.service.*;
import com.nifilili.core.enums.business.BusinessStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessOnboardingControllerTest {

    @Mock private BusinessOnboardingService businessOnboardingService;
    @Mock private BusinessProfileService businessProfileService;
    @Mock private BusinessCategoryService businessCategoryService;
    @Mock private BusinessSectionService businessSectionService;
    @Mock private BusinessAttributeService businessAttributeService;
    @Mock private VerticalDefinitionService verticalDefinitionService;
    @Mock private CategoryDefinitionService categoryDefinitionService;
    @Mock private SectionDefinitionService sectionDefinitionService;
    @Mock private AttributeDefinitionService attributeDefinitionService;
    @Mock private DocumentDefinitionService documentDefinitionService;
    @Mock private BusinessPublishService businessPublishService;

    @InjectMocks
    private BusinessOnboardingController controller;

    @Test
    void configurationEndpoints_WhenDefinitionsExist_ShouldReturnServicePayloads() {
        List<VerticalResponse> verticals = List.of(new VerticalResponse());
        List<CategoryResponse> categories = List.of(new CategoryResponse());
        List<SectionResponse> sections = List.of(SectionResponse.builder().build());
        List<SectionFieldResponse> fields = List.of(new SectionFieldResponse());
        List<AttributeDefinitionResponse> attributes = List.of(new AttributeDefinitionResponse());
        List<DocumentDefinitionResponse> documents = List.of(new DocumentDefinitionResponse());

        when(verticalDefinitionService.getAllActive()).thenReturn(verticals);
        when(categoryDefinitionService.getByVertical(11L)).thenReturn(categories);
        when(categoryDefinitionService.getAll()).thenReturn(categories);
        when(sectionDefinitionService.getByVertical(11L)).thenReturn(sections);
        when(sectionDefinitionService.getFields(55L)).thenReturn(fields);
        when(attributeDefinitionService.getByVertical(11L)).thenReturn(attributes);
        when(documentDefinitionService.getByVertical(11L)).thenReturn(documents);

        assertSame(verticals, controller.getAllActiveVerticals());
        assertSame(categories, controller.getCategoriesByVertical(11L));
        assertSame(categories, controller.getAllCategories());
        assertSame(sections, controller.getSectionsByVertical(11L));
        assertSame(fields, controller.getSectionFields(55L));
        assertSame(attributes, controller.getAttributesByVertical(11L));
        assertSame(documents, controller.getDocumentByVertical(11L));
    }

    @Test
    void createBusiness_WhenServiceReturnsId_ShouldReturnCreatedResponseWithDraftStatus() {
        CreateBusinessRequest request = new CreateBusinessRequest();
        when(businessOnboardingService.createBusiness(request)).thenReturn(101L);

        ResponseEntity<CreateBusinessResponse> response = controller.createBusiness(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(101L, response.getBody().getBusinessId());
        assertEquals(BusinessStatus.DRAFT, response.getBody().getStatus());
    }

    @Test
    void updateAndSubmissionEndpoints_WhenPayloadIsValid_ShouldDelegateAndReturnExpectedStatuses() {
        UpdateBusinessProfileRequest profileRequest = new UpdateBusinessProfileRequest();
        UpdateBusinessCategoriesRequest categoriesRequest = new UpdateBusinessCategoriesRequest();
        categoriesRequest.setCategoryIds(List.of(10L, 20L));
        SaveSectionDataRequest sectionRequest = new SaveSectionDataRequest();
        SaveBusinessAttributeRequest attributeRequest = new SaveBusinessAttributeRequest();
        UploadBusinessDocumentRequest uploadRequest = new UploadBusinessDocumentRequest();
        SubmitBusinessForReviewRequest submitRequest = new SubmitBusinessForReviewRequest();

        assertEquals(HttpStatus.NO_CONTENT, controller.updateBusinessProfile(7L, profileRequest).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.updateBusinessCategories(7L, categoriesRequest).getStatusCode());
        assertEquals(HttpStatus.CREATED, controller.saveSectionData(7L, 8L, sectionRequest).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.saveAttributeData(7L, attributeRequest).getStatusCode());
        assertEquals(HttpStatus.ACCEPTED, controller.uploadDocument(7L, uploadRequest).getStatusCode());
        assertEquals(HttpStatus.ACCEPTED, controller.submitForReview(7L, submitRequest).getStatusCode());

        verify(businessProfileService).updateProfile(7L, profileRequest);
        verify(businessCategoryService).updateCategories(7L, List.of(10L, 20L));
        verify(businessSectionService).saveSectionData(7L, 8L, sectionRequest);
        verify(businessAttributeService).saveAttributeData(7L, attributeRequest);
        verify(businessPublishService).uploadVerificationDocuments(7L, uploadRequest);
        verify(businessPublishService).submitForVerification(7L, submitRequest);
    }

    @Test
    void getLoggedInUserBusinesses_WhenPayloadIsValid_ShouldDelegateAndReturnExpectedStatuses() {
        Pageable  pageable = PageRequest.of(0, 10);
        List<UserCreatedBusinessResponse> businesses = List.of(new UserCreatedBusinessResponse());

        when(businessOnboardingService.getLoggedInUserBusinesses(pageable)).thenReturn(businesses);
        List<UserCreatedBusinessResponse> response = controller.getLoggedInUserBusiness(pageable);
        assertEquals(businesses, response);
    }
}
