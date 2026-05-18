package com.nifilili.business.service.impl;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.business.domain.Business;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.kyc.events.KycApprovedEvent;
import com.nifilili.kyc.events.KycRejectedEvent;
import com.nifilili.kyc.events.KycSubmittedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessKycEventListenerTest {

    @Mock private BusinessRepository businessRepository;

    @InjectMocks
    private BusinessKycEventListener businessKycEventListener;

    @Test
    void onKycSubmitted_WhenBusinessExists_ShouldSetStatusToPending() {
        Business business = TestEntityIdUtil.withId(new Business(), 1L);
        business.setStatus(BusinessStatus.DRAFT);
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));
        when(businessRepository.save(business)).thenReturn(business);

        businessKycEventListener.onKycSubmitted(new KycSubmittedEvent(1L));

        ArgumentCaptor<Business> captor = ArgumentCaptor.forClass(Business.class);
        verify(businessRepository).save(captor.capture());
        assertEquals(BusinessStatus.DRAFT, captor.getValue().getStatus());
    }

    @Test
    void onKycApproved_WhenBusinessExists_ShouldSetStatusToPublished() {
        Business business = TestEntityIdUtil.withId(new Business(), 2L);
        business.setStatus(BusinessStatus.PUBLISHED);
        when(businessRepository.findById(2L)).thenReturn(Optional.of(business));
        when(businessRepository.save(business)).thenReturn(business);

        businessKycEventListener.onKycApproved(new KycApprovedEvent(2L));

        ArgumentCaptor<Business> captor = ArgumentCaptor.forClass(Business.class);
        verify(businessRepository).save(captor.capture());
        assertEquals(BusinessStatus.PUBLISHED, captor.getValue().getStatus());
    }

    @Test
    void onKycRejected_WhenBusinessExists_ShouldSetStatusToDraft() {
        Business business = TestEntityIdUtil.withId(new Business(), 3L);
        business.setStatus(BusinessStatus.DRAFT);
        when(businessRepository.findById(3L)).thenReturn(Optional.of(business));
        when(businessRepository.save(business)).thenReturn(business);

        businessKycEventListener.onKycRejected(new KycRejectedEvent(3L, "Documents expired"));

        ArgumentCaptor<Business> captor = ArgumentCaptor.forClass(Business.class);
        verify(businessRepository).save(captor.capture());
        assertEquals(BusinessStatus.DRAFT, captor.getValue().getStatus());
    }

    @Test
    void onKycSubmitted_WhenBusinessNotFound_ShouldThrowResourceNotFound() {
        when(businessRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> businessKycEventListener.onKycSubmitted(new KycSubmittedEvent(99L)));
        verify(businessRepository, never()).save(any());
    }

    @Test
    void onKycApproved_WhenBusinessNotFound_ShouldThrowResourceNotFound() {
        when(businessRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> businessKycEventListener.onKycApproved(new KycApprovedEvent(99L)));
        verify(businessRepository, never()).save(any());
    }

    @Test
    void onKycRejected_WhenBusinessNotFound_ShouldThrowResourceNotFound() {
        when(businessRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> businessKycEventListener.onKycRejected(new KycRejectedEvent(99L, "Bad docs")));
        verify(businessRepository, never()).save(any());
    }
}
