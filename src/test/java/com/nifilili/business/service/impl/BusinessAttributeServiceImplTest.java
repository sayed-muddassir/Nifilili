package com.nifilili.business.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.domain.AttributeDefinition;
import com.nifilili.business.domain.Business;
import com.nifilili.business.dto.request.SaveBusinessAttributeRequest;
import com.nifilili.business.repository.AttributeDefinitionRepository;
import com.nifilili.business.repository.BusinessAttributeRepository;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessAttributeServiceImplTest {

    @Mock
    private AttributeDefinitionRepository definitionRepository;
    @Mock
    private BusinessAttributeRepository businessAttributeRepository;
    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private BusinessAttributeServiceImpl businessAttributeService;

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    @Test
    void saveAttributeData_WhenDropdownValueIsValid_ShouldSaveAttribute() {
        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 1L);
        business.setOwnerUserId(200L);
        business.setStatus(BusinessStatus.DRAFT);

        AttributeDefinition definition = new AttributeDefinition();
        com.nifilili.business.TestEntityIdUtil.withId(definition, 9L);
        definition.setType("DROPDOWN");
        definition.setOptions(List.of("SMALL", "LARGE"));

        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));
        when(definitionRepository.findById(9L)).thenReturn(Optional.of(definition));

        SaveBusinessAttributeRequest request = new SaveBusinessAttributeRequest();
        request.setAttributeId(9L);
        request.setAttributeValue("SMALL");

        SecurityContextTestUtil.setAuthenticatedUser(200L);
        businessAttributeService.saveAttributeData(1L, request);

        verify(businessAttributeRepository).deleteByBusinessIdAndAttributeId(1L, 9L);
        verify(businessAttributeRepository).save(any());
    }

    @Test
    void saveAttributeData_WhenBusinessIsNotEditable_ShouldThrowValidationError() {
        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 1L);
        business.setOwnerUserId(200L);
        business.setStatus(BusinessStatus.PUBLISHED);
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));

        SaveBusinessAttributeRequest request = new SaveBusinessAttributeRequest();
        request.setAttributeId(9L);
        request.setAttributeValue(true);

        SecurityContextTestUtil.setAuthenticatedUser(200L);
        assertThrows(InvalidBusinessStateException.class,
                () -> businessAttributeService.saveAttributeData(1L, request));

        verify(businessAttributeRepository, never()).save(any());
    }

    @Test
    void saveAttributeData_WhenBusinessOrDefinitionMissing_ShouldThrowNotFound() {
        when(businessRepository.findById(1L)).thenReturn(Optional.empty());

        SecurityContextTestUtil.setAuthenticatedUser(200L);
        SaveBusinessAttributeRequest request = new SaveBusinessAttributeRequest();
        request.setAttributeId(9L);
        assertThrows(ResourceNotFoundException.class, () -> businessAttributeService.saveAttributeData(1L, request));

        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 1L);
        business.setOwnerUserId(200L);
        business.setStatus(BusinessStatus.DRAFT);
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));
        when(definitionRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> businessAttributeService.saveAttributeData(1L, request));
    }

    @Test
    void saveAttributeData_WhenValueTypeIsInvalidOrUnauthorized_ShouldThrowValidationError() {
        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 1L);
        business.setOwnerUserId(200L);
        business.setStatus(BusinessStatus.DRAFT);

        AttributeDefinition textDefinition = new AttributeDefinition();
        textDefinition.setType("TEXT");
        textDefinition.setRequired(true);

        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));
        when(definitionRepository.findById(9L)).thenReturn(Optional.of(textDefinition));

        SaveBusinessAttributeRequest request = new SaveBusinessAttributeRequest();
        request.setAttributeId(9L);
        request.setAttributeValue(123);

        SecurityContextTestUtil.setAuthenticatedUser(200L);
        assertThrows(IllegalArgumentException.class, () -> businessAttributeService.saveAttributeData(1L, request));

        SecurityContextTestUtil.setAuthenticatedUser(201L);
        assertThrows(InvalidBusinessStateException.class, () -> businessAttributeService.saveAttributeData(1L, request));
    }
}
