package com.nifilili.business.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.domain.Business;
import com.nifilili.business.dto.request.CreateBusinessRequest;
import com.nifilili.business.events.BusinessCreatedEvent;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.business.service.BusinessCategoryService;
import com.nifilili.core.enums.business.BusinessSource;
import com.nifilili.core.enums.business.BusinessStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessOnboardingServiceImplTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    BusinessCategoryService businessCategoryService;

    @InjectMocks
    private BusinessOnboardingServiceImpl businessOnboardingService;

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    @Test
    void createBusiness_WhenRequestIsValid_ShouldPersistDraftBusinessAndPublishEvent() {
        CreateBusinessRequest request = new CreateBusinessRequest();
        request.setVerticalId(10L);
        request.setName("Nifilili Cafe");
        request.setLegalName("Nifilili Cafe Pvt Ltd");
        request.setMunicipalityId(5L);
        request.setWardNumber(2);
        request.setToleName("Downtown");
        request.setAddressField1("Main street");
        request.setAddressField2("Near clock tower");
        request.setPostalCode("12345");
        request.setWebsite("https://nifilili.com");
        request.setContacts(Map.of("phone", "1234567890", "email", "nifilili.com"));
        request.setBusinessHours(Map.of("Timings", "9 AM - 9 PM"));
        request.setCategoryIds(List.of(1L));

        SecurityContextTestUtil.setAuthenticatedUser(99L);

        when(businessRepository.save(any(Business.class))).thenAnswer(invocation -> {
            Business business = invocation.getArgument(0);
            com.nifilili.business.TestEntityIdUtil.withId(business, 1001L);
            return business;
        });

        Long businessId = businessOnboardingService.createBusiness(request);

        assertEquals(1001L, businessId);

        ArgumentCaptor<Business> businessCaptor = ArgumentCaptor.forClass(Business.class);
        verify(businessRepository).save(businessCaptor.capture());

        Business savedBusiness = businessCaptor.getValue();
        assertEquals(99L, savedBusiness.getOwnerUserId());
        assertEquals(BusinessStatus.DRAFT, savedBusiness.getStatus());
        assertEquals(BusinessSource.USER_REGISTERED, savedBusiness.getSource());
        assertTrue(savedBusiness.isClaimed());
        assertEquals(BigDecimal.ZERO, savedBusiness.getLatitude());
        assertEquals(BigDecimal.ZERO, savedBusiness.getLongitude());
        assertEquals("Near clock tower", savedBusiness.getAddressField2());
        assertEquals("Nifilili Cafe Pvt Ltd", savedBusiness.getLegalName());

        ArgumentCaptor<BusinessCreatedEvent> eventCaptor = ArgumentCaptor.forClass(BusinessCreatedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(1001L, eventCaptor.getValue().businessId());
    }
}
