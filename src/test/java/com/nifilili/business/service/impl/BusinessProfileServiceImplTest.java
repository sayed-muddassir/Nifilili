package com.nifilili.business.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.domain.Business;
import com.nifilili.business.dto.request.UpdateBusinessProfileRequest;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.InvalidBusinessStateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessProfileServiceImplTest {

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private BusinessProfileServiceImpl businessProfileService;

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    @Test
    void updateProfile_WhenBusinessIsEditableAndOwned_ShouldPersistProfileUpdates() {
        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 1L);
        business.setOwnerUserId(77L);
        business.setStatus(BusinessStatus.PUBLISHED);

        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));

        UpdateBusinessProfileRequest request = new UpdateBusinessProfileRequest();
        request.setBusinessSummary("Updated summary");
        request.setLegalName("Legal Name");
        request.setAddressField2("Landmark");
        request.setContacts(Map.of("phone", "123"));
        request.setBusinessHours(Map.of("sun", "9-5"));
        request.setLatitude(new BigDecimal("27.7000000"));
        request.setLongitude(new BigDecimal("85.3000000"));

        SecurityContextTestUtil.setAuthenticatedUser(77L);
        businessProfileService.updateProfile(1L, request);

        assertEquals("Updated summary", business.getBusinessSummary());
        assertEquals("Legal Name", business.getLegalName());
        assertEquals("Landmark", business.getAddressField2());
        verify(businessRepository).save(business);
    }

    @Test
    void updateProfile_WhenBusinessIsDraft_ShouldRejectUpdate() {
        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 1L);
        business.setOwnerUserId(77L);
        business.setStatus(BusinessStatus.DRAFT);
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));

        SecurityContextTestUtil.setAuthenticatedUser(77L);
        assertThrows(InvalidBusinessStateException.class,
                () -> businessProfileService.updateProfile(1L, new UpdateBusinessProfileRequest()));

        verify(businessRepository, never()).save(any());
    }
}
