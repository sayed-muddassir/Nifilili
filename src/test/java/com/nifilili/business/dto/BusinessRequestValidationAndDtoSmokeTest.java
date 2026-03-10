package com.nifilili.business.dto;

import com.nifilili.business.dto.request.*;
import com.nifilili.business.dto.response.*;
import com.nifilili.core.enums.business.BusinessSectionFieldType;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.enums.kyc.KycStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BusinessRequestValidationAndDtoSmokeTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createBusinessRequest_WhenMandatoryFieldsMissing_ShouldFailValidation() {
        CreateBusinessRequest invalidRequest = new CreateBusinessRequest();
        assertFalse(validator.validate(invalidRequest).isEmpty());

        CreateBusinessRequest validRequest = new CreateBusinessRequest();
        validRequest.setVerticalId(1L);
        validRequest.setName("Nifilili");
        validRequest.setMunicipalityId(2L);
        validRequest.setWardNumber(3);
        validRequest.setToleName("Downtown");
        validRequest.setAddressField1("Main street");
        assertTrue(validator.validate(validRequest).isEmpty());
    }

    @Test
    void collectionConstrainedRequests_WhenEmpty_ShouldFailValidation() {
        UpdateBusinessCategoriesRequest categoriesRequest = new UpdateBusinessCategoriesRequest();
        categoriesRequest.setCategoryIds(List.of());
        assertFalse(validator.validate(categoriesRequest).isEmpty());

        SaveSectionDataRequest sectionDataRequest = new SaveSectionDataRequest();
        sectionDataRequest.setFieldValues(null);
        assertFalse(validator.validate(sectionDataRequest).isEmpty());
    }

    @Test
    void requestAndResponseDtos_WhenValuesAssigned_ShouldExposeThemThroughAccessors() {
        CreateAttributeDefinitionRequest createAttributeDefinitionRequest = new CreateAttributeDefinitionRequest();
        createAttributeDefinitionRequest.setName("has_wifi");
        createAttributeDefinitionRequest.setOptions(List.of("YES", "NO"));

        CreateCategoryRequest createCategoryRequest = new CreateCategoryRequest();
        createCategoryRequest.setName("Cafe");

        CreateDocumentDefinitionRequest createDocumentDefinitionRequest = new CreateDocumentDefinitionRequest();
        createDocumentDefinitionRequest.setAllowedExtensions(List.of("pdf"));

        CreateSectionFieldRequest createSectionFieldRequest = new CreateSectionFieldRequest();
        createSectionFieldRequest.setType(BusinessSectionFieldType.valueOf("TEXT"));

        CreateSectionRequest createSectionRequest = new CreateSectionRequest();
        createSectionRequest.setPromptText("Prompt");

        CreateVerticalRequest createVerticalRequest = new CreateVerticalRequest();
        createVerticalRequest.setSlug("food");

        SaveBusinessAttributeRequest saveBusinessAttributeRequest = new SaveBusinessAttributeRequest();
        saveBusinessAttributeRequest.setAttributeValue(true);

        SubmitBusinessForReviewRequest submitBusinessForReviewRequest = new SubmitBusinessForReviewRequest();
        submitBusinessForReviewRequest.setMessage("review this");

        UpdateBusinessProfileRequest updateBusinessProfileRequest = new UpdateBusinessProfileRequest();
        updateBusinessProfileRequest.setContacts(Map.of("phone", "123"));

        UploadBusinessDocumentRequest uploadBusinessDocumentRequest = new UploadBusinessDocumentRequest();
        uploadBusinessDocumentRequest.setFileName("doc.pdf");

        AttributeDefinitionResponse attributeDefinitionResponse = new AttributeDefinitionResponse();
        attributeDefinitionResponse.setName("wifi");

        BusinessAttributeResponse businessAttributeResponse = BusinessAttributeResponse.builder()
                .attributeId(10L)
                .name("has_wifi")
                .attributeValue(Map.of("value", true))
                .build();

        BusinessResponse businessResponse = BusinessResponse.builder()
                .id(1L)
                .name("Cafe")
                .status(BusinessStatus.PUBLISHED)
                .build();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setName("Cafe");

        CreateBusinessResponse createBusinessResponse = new CreateBusinessResponse(100L, BusinessStatus.DRAFT, "created");

        DocumentDefinitionResponse documentDefinitionResponse = new DocumentDefinitionResponse();
        documentDefinitionResponse.setName("PAN");

        SectionFieldResponse sectionFieldResponse = new SectionFieldResponse();
        sectionFieldResponse.setLabel("Phone");

        SectionResponse sectionResponse = SectionResponse.builder().id(11L).name("General").build();

        SubmitBusinessResponse submitBusinessResponse = new SubmitBusinessResponse(
                100L, BusinessStatus.PENDING, KycStatus.PENDING, "submitted"
        );

        VerticalResponse verticalResponse = new VerticalResponse();
        verticalResponse.setName("Food");

        assertEquals("has_wifi", createAttributeDefinitionRequest.getName());
        assertEquals("Cafe", createCategoryRequest.getName());
        assertEquals(List.of("pdf"), createDocumentDefinitionRequest.getAllowedExtensions());
        assertEquals("TEXT", createSectionFieldRequest.getType().name());
        assertEquals("Prompt", createSectionRequest.getPromptText());
        assertEquals("food", createVerticalRequest.getSlug());
        assertEquals(true, saveBusinessAttributeRequest.getAttributeValue());
        assertEquals("review this", submitBusinessForReviewRequest.getMessage());
        assertEquals(Map.of("phone", "123"), updateBusinessProfileRequest.getContacts());
        assertEquals("doc.pdf", uploadBusinessDocumentRequest.getFileName());
        assertEquals("wifi", attributeDefinitionResponse.getName());
        assertEquals(10L, businessAttributeResponse.getAttributeId());
        assertEquals(1L, businessResponse.getId());
        assertEquals("Cafe", categoryResponse.getName());
        assertEquals(100L, createBusinessResponse.getBusinessId());
        assertEquals("PAN", documentDefinitionResponse.getName());
        assertEquals("Phone", sectionFieldResponse.getLabel());
        assertEquals(11L, sectionResponse.getId());
        assertEquals("Food", verticalResponse.getName());

        // SubmitBusinessResponse intentionally has constructor-only fields (no Lombok @Data).
        assertEquals(100L, ReflectionTestUtils.getField(submitBusinessResponse, "businessId"));
        assertEquals(BusinessStatus.PENDING, ReflectionTestUtils.getField(submitBusinessResponse, "status"));
        assertEquals(KycStatus.PENDING, ReflectionTestUtils.getField(submitBusinessResponse, "kycStatus"));
    }
}
