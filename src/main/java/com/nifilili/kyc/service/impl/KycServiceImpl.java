package com.nifilili.kyc.service.impl;

import com.nifilili.business.domain.Business;
import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;
import com.nifilili.business.events.BusinessClaimedEvent;
import com.nifilili.business.events.BusinessDocumentReviewRequestedEvent;
import com.nifilili.business.events.BusinessPublishRequestedEvent;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.enums.business.DocumentStatus;
import com.nifilili.core.enums.kyc.KycStatus;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.kyc.domain.BusinessDocument;
import com.nifilili.kyc.domain.BusinessKyc;
import com.nifilili.kyc.domain.BusinessKycHistory;
import com.nifilili.kyc.dto.request.DocumentReviewDecisionRequest;
import com.nifilili.kyc.dto.request.ReviewKycRequest;
import com.nifilili.kyc.repository.BusinessDocumentRepository;
import com.nifilili.kyc.repository.BusinessKycHistoryRepository;
import com.nifilili.kyc.repository.BusinessKycRepository;
import com.nifilili.kyc.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
//@PreAuthorize(value = "hasRole('ADMIN')")
public class KycServiceImpl implements KycService {

    private final BusinessRepository businessRepository;
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
    }

    @Override
    @EventListener
    public void submit(BusinessPublishRequestedEvent event) {
        Long businessId = event.businessId();
        String message = event.message();

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

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

        business.setStatus(BusinessStatus.PENDING);
        businessRepository.save(business);
    }

    @Override
    public void review(Long businessId, ReviewKycRequest request) {

        BusinessKyc kyc = kycRepository.findByBusinessId(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC not found"));

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

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        if (status == KycStatus.APPROVED) {
            business.setStatus(BusinessStatus.PUBLISHED);
        } else {
            business.setStatus(BusinessStatus.DRAFT);
        }

        businessRepository.save(business);
    }

    @Override
    @EventListener
    public void onBusinessClaimed(BusinessClaimedEvent event) {
        Long businessId = event.businessId();

        BusinessKyc kyc = new BusinessKyc();
        kyc.setBusinessId(businessId);
        kyc.setKycStatus(KycStatus.NOT_STARTED);
        kyc.setSubmissionCount(0);
        kyc.setCreatedAt(Instant.now());
        kyc.setUpdatedAt(Instant.now());

        kycRepository.save(kyc);
        saveHistory(businessId, KycStatus.NOT_STARTED, "Business claimed — awaiting document upload");
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
