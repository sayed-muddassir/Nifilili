package com.nifilili.business.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.domain.*;
import com.nifilili.business.dto.request.SaveSectionDataRequest;
import com.nifilili.business.repository.*;
import com.nifilili.business.validation.SectionValidationService;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.InvalidBusinessStateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessSectionServiceImplTest {

    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private BusinessSectionDataRepository businessSectionDataRepository;
    @Mock
    private SectionGroupRepository sectionGroupRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private SectionFieldRepository sectionFieldRepository;
    @Mock
    private BusinessCategoryRepository businessCategoryRepository;
    @Mock
    private SectionValidationService sectionValidationService;

    @InjectMocks
    private BusinessSectionServiceImpl businessSectionService;

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    @Test
    void saveSectionData_WhenRepeatableAndValid_ShouldCreateGroupAndSaveData() {
        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 5L);
        business.setOwnerUserId(7L);
        business.setVerticalId(1L);
        business.setStatus(BusinessStatus.DRAFT);

        SectionDefinition sectionDefinition = new SectionDefinition();
        com.nifilili.business.TestEntityIdUtil.withId(sectionDefinition, 11L);
        sectionDefinition.setVerticalId(1L);
        sectionDefinition.setAllowMultiple(true);

        when(businessRepository.findById(5L)).thenReturn(Optional.of(business));
        when(sectionRepository.findById(11L)).thenReturn(Optional.of(sectionDefinition));
        when(sectionFieldRepository.findBySectionId(11L)).thenReturn(List.of());
        when(sectionGroupRepository.save(any(BusinessSectionGroup.class))).thenAnswer(invocation -> {
            BusinessSectionGroup group = invocation.getArgument(0);
            com.nifilili.business.TestEntityIdUtil.withId(group, 99L);
            return group;
        });

        SaveSectionDataRequest request = new SaveSectionDataRequest();
        request.setFieldValues(Map.of("title", "Sample"));

        SecurityContextTestUtil.setAuthenticatedUser(7L);
        businessSectionService.saveSectionData(5L, 11L, request);

        verify(sectionGroupRepository).save(any(BusinessSectionGroup.class));
        verify(businessSectionDataRepository).save(any(BusinessSectionData.class));
    }

    @Test
    void saveSectionData_WhenSectionCategoryNotAssignedToBusiness_ShouldThrowValidationError() {
        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 5L);
        business.setOwnerUserId(7L);
        business.setVerticalId(1L);
        business.setStatus(BusinessStatus.DRAFT);

        SectionDefinition sectionDefinition = new SectionDefinition();
        com.nifilili.business.TestEntityIdUtil.withId(sectionDefinition, 11L);
        sectionDefinition.setVerticalId(1L);
        sectionDefinition.setCategoryId(500L);
        sectionDefinition.setAllowMultiple(false);

        when(businessRepository.findById(5L)).thenReturn(Optional.of(business));
        when(sectionRepository.findById(11L)).thenReturn(Optional.of(sectionDefinition));
        when(businessCategoryRepository.findByBusinessId(5L)).thenReturn(List.of());

        SaveSectionDataRequest request = new SaveSectionDataRequest();
        request.setFieldValues(Map.of("title", "Sample"));

        SecurityContextTestUtil.setAuthenticatedUser(7L);
        assertThrows(InvalidBusinessStateException.class,
                () -> businessSectionService.saveSectionData(5L, 11L, request));

        verify(businessSectionDataRepository, never()).save(any());
    }
}
