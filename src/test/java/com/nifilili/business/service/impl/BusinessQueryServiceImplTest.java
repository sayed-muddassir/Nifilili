package com.nifilili.business.service.impl;

import com.nifilili.business.domain.*;
import com.nifilili.business.dto.response.BusinessResponse;
import com.nifilili.business.repository.*;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessQueryServiceImplTest {

    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private BusinessCategoryRepository businessCategoryRepository;
    @Mock
    private BusinessSectionDataRepository businessDataRepository;
    @Mock
    private BusinessAttributeRepository businessAttributeRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private AttributeDefinitionRepository attributeDefinitionRepository;

    @InjectMocks
    private BusinessQueryServiceImpl businessQueryService;

    @Test
    void getBusinessById_WhenPublishedBusinessExists_ShouldReturnDetailedResponse() {
        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 1L);
        business.setName("Cafe");
        business.setLegalName("Cafe Pvt");
        business.setVerticalId(1L);
        business.setStatus(BusinessStatus.PUBLISHED);
        business.setLatitude(new BigDecimal("27.7000000"));
        business.setLongitude(new BigDecimal("85.3000000"));
        business.setAverageRating(new BigDecimal("4.50"));
        business.setReviewCount(12);

        SectionDefinition section = new SectionDefinition();
        com.nifilili.business.TestEntityIdUtil.withId(section, 10L);
        section.setName("Menu");

        AttributeDefinition attributeDefinition = new AttributeDefinition();
        com.nifilili.business.TestEntityIdUtil.withId(attributeDefinition, 20L);
        attributeDefinition.setName("hasParking");

        BusinessAttribute businessAttribute = new BusinessAttribute();
        businessAttribute.setAttributeId(20L);
        businessAttribute.setAttributeValue(Map.of("value", true));

        BusinessSectionData businessSectionData = new BusinessSectionData();
        businessSectionData.setSectionId(10L);
        businessSectionData.setFieldValues(Map.of("item", "Coffee"));

        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));
        when(businessCategoryRepository.findByBusinessId(1L)).thenReturn(List.of(new BusinessCategory(1L, 100L)));
        when(businessDataRepository.findByBusinessId(1L)).thenReturn(List.of(businessSectionData));
        when(businessAttributeRepository.findByBusinessId(1L)).thenReturn(List.of(businessAttribute));
        when(sectionRepository.findById(10L)).thenReturn(Optional.of(section));
        when(attributeDefinitionRepository.findById(20L)).thenReturn(Optional.of(attributeDefinition));

        BusinessResponse response = businessQueryService.getBusinessById(1L);

        assertEquals("Cafe", response.getName());
        assertEquals("Cafe Pvt", response.getLegalName());
        assertEquals(1, response.getSections().size());
        assertEquals(1, response.getAttributes().size());
    }

    @Test
    void getBusinessById_WhenBusinessIsNotPublished_ShouldThrowNotFoundException() {
        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 1L);
        business.setStatus(BusinessStatus.DRAFT);
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));

        assertThrows(ResourceNotFoundException.class, () -> businessQueryService.getBusinessById(1L));
    }

    @Test
    void getAllBusinesses_WhenPublishedResultsExist_ShouldReturnPublishedPage() {
        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 1L);
        business.setStatus(BusinessStatus.PUBLISHED);
        business.setName("Sample");

        when(businessRepository.findByStatus(BusinessStatus.PUBLISHED, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(business), PageRequest.of(0, 10), 1));
        when(businessCategoryRepository.findByBusinessId(1L)).thenReturn(List.of());
        when(businessDataRepository.findByBusinessId(1L)).thenReturn(List.of());
        when(businessAttributeRepository.findByBusinessId(1L)).thenReturn(List.of());

        assertEquals(1, businessQueryService.getAllBusinesses(PageRequest.of(0, 10)).getTotalElements());
    }
}
