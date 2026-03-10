package com.nifilili.kyc.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;
import com.nifilili.business.events.BusinessClaimedEvent;
import com.nifilili.business.events.BusinessDocumentReviewRequestedEvent;
import com.nifilili.business.events.BusinessPublishRequestedEvent;
import com.nifilili.core.enums.business.DocumentStatus;
import com.nifilili.core.enums.kyc.KycStatus;
import com.nifilili.core.exception.InvalidKycStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.kyc.domain.BusinessDocument;
import com.nifilili.kyc.domain.BusinessKyc;
import com.nifilili.kyc.dto.request.ReviewKycRequest;
import com.nifilili.kyc.events.KycApprovedEvent;
import com.nifilili.kyc.events.KycRejectedEvent;
import com.nifilili.kyc.events.KycSubmittedEvent;
import com.nifilili.kyc.repository.BusinessDocumentRepository;
import com.nifilili.kyc.repository.BusinessKycHistoryRepository;
import com.nifilili.kyc.repository.BusinessKycRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycServiceImplTest {

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private BusinessKycRepository kycRepository;

    @Mock
    private BusinessDocumentRepository documentRepository;

    @Mock
    private BusinessKycHistoryRepository historyRepository;

    @InjectMocks
    private KycServiceImpl kycService;

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // ── uploadDocument ──────────────────────────────────────────────────

    @Test
    void uploadDocument_WhenValidEvent_ShouldSaveDocument() {
        UploadBusinessDocumentRequest request = new UploadBusinessDocumentRequest();
        request.setDocumentDefinitionId(3L);
        request.setFileUrl("https://cdn.nifilili.com/docs/pan.pdf");
        request.setFileName("pan.pdf");

        BusinessDocumentReviewRequestedEvent event =
                new BusinessDocumentReviewRequestedEvent(10L, request);

        kycService.uploadDocument(event);

        ArgumentCaptor<BusinessDocument> captor = ArgumentCaptor.forClass(BusinessDocument.class);
        verify(documentRepository).save(captor.capture());

        BusinessDocument saved = captor.getValue();
        assertEquals(10L, saved.getBusinessId());
        assertEquals(3L, saved.getDocumentDefinitionId());
        assertEquals("https://cdn.nifilili.com/docs/pan.pdf", saved.getFileUrl());
        assertEquals("pan.pdf", saved.getFileName());
        assertEquals(DocumentStatus.PENDING, saved.getStatus());
        assertNull(saved.getRejectionReason());
        assertNull(saved.getReviewedByUserId());
        assertNull(saved.getReviewedAt());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    // ── submit ──────────────────────────────────────────────────────────

    @Test
    void submit_WhenNewKyc_ShouldCreateRecordAndPublishEvent() {
        when(kycRepository.findByBusinessId(10L)).thenReturn(Optional.empty());

        BusinessPublishRequestedEvent event =
                new BusinessPublishRequestedEvent(10L, "Please review my business");

        kycService.submit(event);

        ArgumentCaptor<BusinessKyc> kycCaptor = ArgumentCaptor.forClass(BusinessKyc.class);
        verify(kycRepository).save(kycCaptor.capture());

        BusinessKyc saved = kycCaptor.getValue();
        assertEquals(10L, saved.getBusinessId());
        assertEquals(KycStatus.PENDING, saved.getKycStatus());
        assertEquals("Please review my business", saved.getAdminMessage());
        assertNull(saved.getRejectionReason());
        assertEquals(1, saved.getSubmissionCount());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        ArgumentCaptor<KycSubmittedEvent> eventCaptor = ArgumentCaptor.forClass(KycSubmittedEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        assertEquals(10L, eventCaptor.getValue().businessId());
    }

    @Test
    void submit_WhenExistingKyc_ShouldIncrementSubmissionCount() {
        BusinessKyc existingKyc = new BusinessKyc();
        existingKyc.setBusinessId(10L);
        existingKyc.setKycStatus(KycStatus.REJECTED);
        existingKyc.setSubmissionCount(1);
        existingKyc.setCreatedAt(java.time.Instant.now());

        when(kycRepository.findByBusinessId(10L)).thenReturn(Optional.of(existingKyc));

        BusinessPublishRequestedEvent event =
                new BusinessPublishRequestedEvent(10L, "Resubmitting");

        kycService.submit(event);

        ArgumentCaptor<BusinessKyc> kycCaptor = ArgumentCaptor.forClass(BusinessKyc.class);
        verify(kycRepository).save(kycCaptor.capture());

        BusinessKyc saved = kycCaptor.getValue();
        assertEquals(2, saved.getSubmissionCount());
        assertEquals(KycStatus.PENDING, saved.getKycStatus());
        assertNull(saved.getRejectionReason());

        verify(publisher).publishEvent(any(KycSubmittedEvent.class));
    }

    // ── review ──────────────────────────────────────────────────────────

    @Test
    void review_WhenApproved_ShouldSetApprovedAndPublishEvent() {
        long adminUserId = 99L;
        BusinessKyc pendingKyc = new BusinessKyc();
        pendingKyc.setBusinessId(10L);
        pendingKyc.setKycStatus(KycStatus.PENDING);
        pendingKyc.setSubmissionCount(1);

        when(kycRepository.findByBusinessId(10L)).thenReturn(Optional.of(pendingKyc));
        when(documentRepository.findByBusinessId(10L)).thenReturn(Collections.emptyList());

        ReviewKycRequest request = new ReviewKycRequest();
        request.setApprove(true);
        request.setAdminMessage("Looks good");

        SecurityContextTestUtil.setAuthenticatedUser(adminUserId);
        kycService.review(10L, request);

        ArgumentCaptor<BusinessKyc> kycCaptor = ArgumentCaptor.forClass(BusinessKyc.class);
        verify(kycRepository).save(kycCaptor.capture());

        BusinessKyc saved = kycCaptor.getValue();
        assertEquals(KycStatus.APPROVED, saved.getKycStatus());
        assertEquals("Looks good", saved.getAdminMessage());
        assertNull(saved.getRejectionReason());

        ArgumentCaptor<KycApprovedEvent> eventCaptor = ArgumentCaptor.forClass(KycApprovedEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        assertEquals(10L, eventCaptor.getValue().businessId());
    }

    @Test
    void review_WhenRejected_ShouldSetRejectedAndPublishEvent() {
        long adminUserId = 99L;
        BusinessKyc pendingKyc = new BusinessKyc();
        pendingKyc.setBusinessId(10L);
        pendingKyc.setKycStatus(KycStatus.PENDING);
        pendingKyc.setSubmissionCount(1);

        when(kycRepository.findByBusinessId(10L)).thenReturn(Optional.of(pendingKyc));
        when(documentRepository.findByBusinessId(10L)).thenReturn(Collections.emptyList());

        ReviewKycRequest request = new ReviewKycRequest();
        request.setApprove(false);
        request.setAdminMessage("Documents are blurry");

        SecurityContextTestUtil.setAuthenticatedUser(adminUserId);
        kycService.review(10L, request);

        ArgumentCaptor<BusinessKyc> kycCaptor = ArgumentCaptor.forClass(BusinessKyc.class);
        verify(kycRepository).save(kycCaptor.capture());

        BusinessKyc saved = kycCaptor.getValue();
        assertEquals(KycStatus.REJECTED, saved.getKycStatus());
        assertEquals("Documents are blurry", saved.getAdminMessage());
        assertEquals("Documents are blurry", saved.getRejectionReason());

        ArgumentCaptor<KycRejectedEvent> eventCaptor = ArgumentCaptor.forClass(KycRejectedEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        assertEquals(10L, eventCaptor.getValue().businessId());
        assertEquals("Documents are blurry", eventCaptor.getValue().rejectionReason());
    }

    @Test
    void review_WhenKycNotFound_ShouldThrowResourceNotFound() {
        when(kycRepository.findByBusinessId(999L)).thenReturn(Optional.empty());

        ReviewKycRequest request = new ReviewKycRequest();
        request.setApprove(true);

        assertThrows(ResourceNotFoundException.class,
                () -> kycService.review(999L, request));

        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void review_WhenNotPending_ShouldThrowInvalidKycState() {
        BusinessKyc approvedKyc = new BusinessKyc();
        approvedKyc.setBusinessId(10L);
        approvedKyc.setKycStatus(KycStatus.APPROVED);

        when(kycRepository.findByBusinessId(10L)).thenReturn(Optional.of(approvedKyc));

        ReviewKycRequest request = new ReviewKycRequest();
        request.setApprove(true);

        assertThrows(InvalidKycStateException.class,
                () -> kycService.review(10L, request));

        verify(publisher, never()).publishEvent(any());
    }

    // ── onBusinessClaimed ───────────────────────────────────────────────

    @Test
    void onBusinessClaimed_WhenNewClaim_ShouldCreateKycRecord() {
        when(kycRepository.findByBusinessId(50L)).thenReturn(Optional.empty());

        BusinessClaimedEvent event = new BusinessClaimedEvent(50L, 77L);

        kycService.onBusinessClaimed(event);

        ArgumentCaptor<BusinessKyc> kycCaptor = ArgumentCaptor.forClass(BusinessKyc.class);
        verify(kycRepository).save(kycCaptor.capture());

        BusinessKyc saved = kycCaptor.getValue();
        assertEquals(50L, saved.getBusinessId());
        assertEquals(KycStatus.NOT_STARTED, saved.getKycStatus());
        assertEquals(0, saved.getSubmissionCount());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void onBusinessClaimed_WhenKycAlreadyExists_ShouldSkipDuplicate() {
        BusinessKyc existingKyc = new BusinessKyc();
        existingKyc.setBusinessId(50L);
        existingKyc.setKycStatus(KycStatus.NOT_STARTED);

        when(kycRepository.findByBusinessId(50L)).thenReturn(Optional.of(existingKyc));

        BusinessClaimedEvent event = new BusinessClaimedEvent(50L, 77L);

        kycService.onBusinessClaimed(event);

        verify(kycRepository, never()).save(any(BusinessKyc.class));
    }
}
