package com.nifilili.business.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.domain.Business;
import com.nifilili.business.dto.request.SubmitBusinessForReviewRequest;
import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;
import com.nifilili.business.events.BusinessDocumentReviewRequestedEvent;
import com.nifilili.business.events.BusinessPublishRequestedEvent;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.core.exception.InvalidBusinessStateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
