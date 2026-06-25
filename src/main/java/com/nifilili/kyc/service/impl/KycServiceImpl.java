package com.nifilili.kyc.service.impl;

import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;
import com.nifilili.business.events.BusinessClaimedEvent;
import com.nifilili.business.events.BusinessDocumentReviewRequestedEvent;
import com.nifilili.business.events.BusinessPublishRequestedEvent;
import com.nifilili.core.enums.business.DocumentStatus;
import com.nifilili.core.enums.kyc.KycStatus;
import com.nifilili.core.exception.InvalidKycStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.kyc.domain.BusinessDocument;
import com.nifilili.kyc.domain.BusinessKyc;
import com.nifilili.kyc.domain.BusinessKycHistory;
import com.nifilili.kyc.dto.request.DocumentReviewDecisionRequest;
import com.nifilili.kyc.dto.request.ReviewKycRequest;
import com.nifilili.kyc.events.KycApprovedEvent;
import com.nifilili.kyc.events.KycRejectedEvent;
import com.nifilili.kyc.events.KycSubmittedEvent;
import com.nifilili.kyc.repository.BusinessDocumentRepository;
import com.nifilili.kyc.repository.BusinessKycHistoryRepository;
import com.nifilili.kyc.repository.BusinessKycRepository;
import com.nifilili.kyc.service.KycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KycServiceImpl implements KycService {

    private final ApplicationEventPublisher publisher;
    private final BusinessKycRepository kycRepository;
    private final BusinessDocumentRepository documentRepository;
    private final BusinessKycHistoryRepository historyRepository;

    @Override
    @EventListener
    public void uploadDocument(BusinessDocumentReviewRequestedEvent event) {
        Long businessId = event.businessId();
        UploadBusinessDocumentRequest request = event.request();

        BusinessDocument document = new BusinessDocument(
                businessId,
                request.getDocumentDefinitionId(),
                request.getFileUrl(),
                request.getFileName(),
                DocumentStatus.PENDING,
                null,
                null,
                null,
                Instant.now(),
                Instant.now()
        );

        documentRepository.save(document);
        log.info("Document uploaded for businessId={}, definitionId={}", businessId, request.getDocumentDefinitionId());
    }

    @Override
    @EventListener
    public void submit(BusinessPublishRequestedEvent event) {
        Long businessId = event.businessId();
        String message = event.message();

        BusinessKyc kyc = kycRepository.findByBusinessId(businessId)
                .orElse(new BusinessKyc());

        kyc.setBusinessId(businessId);
        kyc.setKycStatus(KycStatus.PENDING);
        kyc.setAdminMessage(message);
        kyc.setRejectionReason(null);
        kyc.setSubmissionCount(
                kyc.getSubmissionCount() == null ? 1 : kyc.getSubmissionCount() + 1
        );
        if (kyc.getCreatedAt() == null) {
            kyc.setCreatedAt(Instant.now());
        }
        kyc.setUpdatedAt(Instant.now());

        kycRepository.save(kyc);
        saveHistory(businessId, KycStatus.PENDING, message);

        publisher.publishEvent(new KycSubmittedEvent(businessId));
        log.info("KYC submitted for businessId={}, submissionCount={}", businessId, kyc.getSubmissionCount());
    }

    @Override
    public void review(Long businessId, ReviewKycRequest request) {
        BusinessKyc kyc = kycRepository.findByBusinessId(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC not found for businessId: " + businessId));

        if (kyc.getKycStatus() != KycStatus.PENDING) {
            throw new InvalidKycStateException(
                    "KYC for business " + businessId + " is in state " + kyc.getKycStatus() + ", expected PENDING");
        }

        KycStatus status = request.isApprove()
                ? KycStatus.APPROVED
                : KycStatus.REJECTED;

        kyc.setKycStatus(status);
        kyc.setAdminMessage(request.getAdminMessage());
        kyc.setRejectionReason(status == KycStatus.REJECTED ? request.getAdminMessage() : null);
        kyc.setUpdatedAt(Instant.now());
        kycRepository.save(kyc);

        saveHistory(businessId, status, request.getAdminMessage());
        applyDocumentReviewDecisions(businessId, request, status);

        if (status == KycStatus.APPROVED) {
            publisher.publishEvent(new KycApprovedEvent(businessId));
        } else {
            publisher.publishEvent(new KycRejectedEvent(businessId, request.getAdminMessage()));
        }
        log.info("KYC reviewed for businessId={}, decision={}", businessId, status);
    }

    @Override
    @EventListener
    public void onBusinessClaimed(BusinessClaimedEvent event) {
        Long businessId = event.businessId();

        Optional<BusinessKyc> existing = kycRepository.findByBusinessId(businessId);
        if (existing.isPresent()) {
            log.warn("KYC record already exists for businessId={}, skipping duplicate claim event", businessId);
            return;
        }

        BusinessKyc kyc = new BusinessKyc();
        kyc.setBusinessId(businessId);
        kyc.setAdminMessage("Business Claim process started, documents need to be submitted");
        kyc.setKycStatus(KycStatus.NOT_STARTED);
        kyc.setSubmissionCount(0);
        kyc.setCreatedAt(Instant.now());
        kyc.setUpdatedAt(Instant.now());

        kycRepository.save(kyc);
        saveHistory(businessId, KycStatus.NOT_STARTED, "Business claimed — awaiting document upload");
        log.info("KYC record created for claimed businessId={}", businessId);
    }

    private void applyDocumentReviewDecisions(Long businessId, ReviewKycRequest request, KycStatus kycStatus) {
        List<DocumentReviewDecisionRequest> documentDecisions = request.getDocumentDecisions();
        Long reviewerUserId = resolveReviewerUserId();
        Instant reviewedAt = Instant.now();

        if (documentDecisions != null && !documentDecisions.isEmpty()) {
            for (DocumentReviewDecisionRequest documentDecision : documentDecisions) {
                BusinessDocument businessDocument = documentRepository
                        .findByIdAndBusinessId(documentDecision.getBusinessDocumentId(), businessId)
                        .orElseThrow(() -> new ResourceNotFoundException("Business document not found"));

                updateBusinessDocumentReviewOutcome(
                        businessDocument,
                        documentDecision.isApprove(),
                        documentDecision.getRejectionReason(),
                        reviewerUserId,
                        reviewedAt
                );
            }
            return;
        }

        DocumentStatus fallbackDocumentStatus = kycStatus == KycStatus.APPROVED
                ? DocumentStatus.APPROVED
                : DocumentStatus.REJECTED;

        String fallbackRejectionReason = kycStatus == KycStatus.REJECTED
                ? request.getAdminMessage()
                : null;

        List<BusinessDocument> allBusinessDocuments = documentRepository.findByBusinessId(businessId);
        for (BusinessDocument businessDocument : allBusinessDocuments) {
            updateBusinessDocumentReviewOutcome(
                    businessDocument,
                    fallbackDocumentStatus == DocumentStatus.APPROVED,
                    fallbackRejectionReason,
                    reviewerUserId,
                    reviewedAt
            );
        }
    }

    private void updateBusinessDocumentReviewOutcome(
            BusinessDocument businessDocument,
            boolean approved,
            String rejectionReason,
            Long reviewerUserId,
            Instant reviewedAt
    ) {
        businessDocument.setStatus(approved ? DocumentStatus.APPROVED : DocumentStatus.REJECTED);
        businessDocument.setRejectionReason(approved ? null : rejectionReason);
        businessDocument.setReviewedByUserId(reviewerUserId);
        businessDocument.setReviewedAt(reviewedAt);
        businessDocument.setUpdatedAt(Instant.now());
        documentRepository.save(businessDocument);
    }

    private Long resolveReviewerUserId() {
        try {
            return SecurityUtil.getCurrentUserId();
        } catch (Exception ignored) {
            // Event-driven flows may execute without an authenticated context.
            return null;
        }
    }

    private void saveHistory(Long businessId, KycStatus status, String message) {
        historyRepository.save(
                new BusinessKycHistory(
                        businessId,
                        status,
                        message,
                        Instant.now(),
                        Instant.now()
                )
        );
    }
}
