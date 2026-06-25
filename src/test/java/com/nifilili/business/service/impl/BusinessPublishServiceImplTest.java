package com.nifilili.business.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.business.domain.Business;
import com.nifilili.business.dto.request.SubmitBusinessForReviewRequest;
import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;
import com.nifilili.business.events.BusinessClaimedEvent;
import com.nifilili.business.events.BusinessDocumentReviewRequestedEvent;
import com.nifilili.business.events.BusinessPublishRequestedEvent;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.core.enums.business.BusinessSource;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessPublishServiceImplTest {

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private BusinessPublishServiceImpl businessPublishService;

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    @Test
    void uploadVerificationDocuments_WhenOwnerMatches_ShouldPublishDocumentReviewEvent() {
        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 10L);
        business.setOwnerUserId(88L);
        when(businessRepository.findById(10L)).thenReturn(Optional.of(business));

        UploadBusinessDocumentRequest request = new UploadBusinessDocumentRequest();
        request.setDocumentDefinitionId(3L);

        SecurityContextTestUtil.setAuthenticatedUser(88L);
        businessPublishService.uploadVerificationDocuments(10L, request);

        ArgumentCaptor<BusinessDocumentReviewRequestedEvent> eventCaptor = ArgumentCaptor.forClass(BusinessDocumentReviewRequestedEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        assertEquals(10L, eventCaptor.getValue().businessId());
        assertEquals(3L, eventCaptor.getValue().request().getDocumentDefinitionId());
    }

    @Test
    void submitForVerification_WhenOwnerDoesNotMatch_ShouldThrowUnauthorizedException() {
        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 10L);
        business.setOwnerUserId(88L);
        when(businessRepository.findById(10L)).thenReturn(Optional.of(business));

        SubmitBusinessForReviewRequest request = new SubmitBusinessForReviewRequest();
        request.setMessage("Please review");

        SecurityContextTestUtil.setAuthenticatedUser(99L);
        assertThrows(InvalidBusinessStateException.class,
                () -> businessPublishService.submitForVerification(10L, request));

        verify(publisher, never()).publishEvent(any(BusinessPublishRequestedEvent.class));
    }

    @Test
    void claimBusiness_WhenAdminSeededAndUnclaimed_ShouldSetOwnerAndPublishEvent() {
        Business business = new Business();
        TestEntityIdUtil.withId(business, 50L);
        business.setSource(BusinessSource.ADMIN_SEEDED);
        business.setClaimed(false);
        when(businessRepository.findById(50L)).thenReturn(Optional.of(business));

        SecurityContextTestUtil.setAuthenticatedUser(77L);
        businessPublishService.claimBusiness(50L);

        assertEquals(77L, business.getOwnerUserId());
        assertEquals(77L, business.getClaimedByUserId());
        assertFalse(business.isClaimed());// Because the isClaimed flag is only set to true after KYC approval, not immediately upon claiming.
        verify(businessRepository).save(business);

        ArgumentCaptor<BusinessClaimedEvent> eventCaptor = ArgumentCaptor.forClass(BusinessClaimedEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        assertEquals(50L, eventCaptor.getValue().businessId());
        assertEquals(77L, eventCaptor.getValue().claimedByUserId());
    }

    @Test
    void claimBusiness_WhenAlreadyClaimed_ShouldThrowInvalidBusinessState() {
        Business business = new Business();
        TestEntityIdUtil.withId(business, 50L);
        business.setSource(BusinessSource.ADMIN_SEEDED);
        business.setClaimed(true);
        when(businessRepository.findById(50L)).thenReturn(Optional.of(business));

        SecurityContextTestUtil.setAuthenticatedUser(77L);
        assertThrows(InvalidBusinessStateException.class,
                () -> businessPublishService.claimBusiness(50L));

        verify(publisher, never()).publishEvent(any(BusinessClaimedEvent.class));
    }

    @Test
    void claimBusiness_WhenUserRegistered_ShouldThrowInvalidBusinessState() {
        Business business = new Business();
        TestEntityIdUtil.withId(business, 50L);
        business.setSource(BusinessSource.USER_REGISTERED);
        business.setClaimed(false);
        when(businessRepository.findById(50L)).thenReturn(Optional.of(business));

        SecurityContextTestUtil.setAuthenticatedUser(77L);
        assertThrows(InvalidBusinessStateException.class,
                () -> businessPublishService.claimBusiness(50L));

        verify(publisher, never()).publishEvent(any(BusinessClaimedEvent.class));
    }

    @Test
    void claimBusiness_WhenBusinessNotFound_ShouldThrowResourceNotFound() {
        when(businessRepository.findById(999L)).thenReturn(Optional.empty());

        SecurityContextTestUtil.setAuthenticatedUser(77L);
        assertThrows(ResourceNotFoundException.class,
                () -> businessPublishService.claimBusiness(999L));

        verify(publisher, never()).publishEvent(any(BusinessClaimedEvent.class));
    }
}
