package com.nifilili.business.domain;

import com.nifilili.business.api.BusinessValidationApi;
import com.nifilili.business.events.BusinessCreatedEvent;
import com.nifilili.business.events.BusinessDocumentReviewRequestedEvent;
import com.nifilili.business.events.BusinessPublishRequestedEvent;
import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;
import com.nifilili.business.validation.FieldType;
import com.nifilili.business.validation.SectionFieldMetadata;
import com.nifilili.business.validation.SectionValidationException;
import com.nifilili.core.enums.business.BusinessSource;
import com.nifilili.core.enums.business.BusinessStatus;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.ApplicationModule;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BusinessDomainAndModuleContractTest {

    @Test
    void domainEntities_WhenValuesAreSet_ShouldExposeSameValues() {
        AttributeDefinition attributeDefinition = new AttributeDefinition();
        attributeDefinition.setName("has_wifi");
        attributeDefinition.setOptions(List.of("YES", "NO"));

        Business business = Business.builder()
                .name("Cafe")
                .status(BusinessStatus.DRAFT)
                .source(BusinessSource.USER_REGISTERED)
                .latitude(new BigDecimal("27.70"))
                .contacts(Map.of("phone", "123"))
                .build();

        BusinessAttribute businessAttribute = new BusinessAttribute();
        businessAttribute.setAttributeId(12L);
        businessAttribute.setAttributeValue(Map.of("value", true));

        BusinessCategory businessCategory = new BusinessCategory(1L, 2L);

        BusinessSectionData businessSectionData = new BusinessSectionData();
        businessSectionData.setFieldValues(Map.of("title", "menu"));

        BusinessSectionGroup businessSectionGroup = new BusinessSectionGroup();
        businessSectionGroup.setName("group-1");

        CategoryDefinition categoryDefinition = new CategoryDefinition();
        categoryDefinition.setName("Cafe");

        DocumentDefinition documentDefinition = new DocumentDefinition();
        documentDefinition.setAllowedExtensions(List.of("pdf"));

        SectionDefinition sectionDefinition = new SectionDefinition();
        sectionDefinition.setPrompt("Prompt");

        SectionField sectionField = new SectionField();
        sectionField.setType("TEXT");

        VerticalDefinition verticalDefinition = new VerticalDefinition();
        verticalDefinition.setName("Food");

        assertEquals("has_wifi", attributeDefinition.getName());
        assertEquals("Cafe", business.getName());
        assertEquals(12L, businessAttribute.getAttributeId());
        assertEquals(2L, businessCategory.getCategoryId());
        assertEquals("menu", businessSectionData.getFieldValues().get("title"));
        assertEquals("group-1", businessSectionGroup.getName());
        assertEquals("Cafe", categoryDefinition.getName());
        assertEquals(List.of("pdf"), documentDefinition.getAllowedExtensions());
        assertEquals("Prompt", sectionDefinition.getPrompt());
        assertEquals("TEXT", sectionField.getType());
        assertEquals("Food", verticalDefinition.getName());
    }

    @Test
    void eventsAndValidationTypes_WhenConstructed_ShouldExposePayloadAndErrors() {
        UploadBusinessDocumentRequest uploadRequest = new UploadBusinessDocumentRequest();
        uploadRequest.setFileName("doc.pdf");

        BusinessCreatedEvent createdEvent = new BusinessCreatedEvent(10L);
        BusinessDocumentReviewRequestedEvent reviewEvent = new BusinessDocumentReviewRequestedEvent(10L, uploadRequest);
        BusinessPublishRequestedEvent publishEvent = new BusinessPublishRequestedEvent(10L, "review");

        SectionFieldMetadata metadata = new SectionFieldMetadata("website", FieldType.MEDIA_URL, true, false, List.of());
        SectionValidationException validationException = new SectionValidationException(List.of("field is required"));

        assertEquals(10L, createdEvent.businessId());
        assertEquals("doc.pdf", reviewEvent.request().getFileName());
        assertEquals("review", publishEvent.message());
        assertEquals(FieldType.MEDIA_URL, metadata.type());
        assertEquals(List.of("field is required"), validationException.getErrors());
    }

    @Test
    void modulePackageAndApiContract_ShouldBeDiscoverableAndCallable() {
        Package businessPackage;
        try {
            businessPackage = Class.forName("com.nifilili.business.package-info").getPackage();
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
        ApplicationModule module = businessPackage.getAnnotation(ApplicationModule.class);

        BusinessValidationApi validationApi = businessId -> businessId != null && businessId > 0;

        assertNotNull(module);
        assertTrue(validationApi.existsAndActive(1L));
        assertFalse(validationApi.existsAndActive(0L));
    }
}
