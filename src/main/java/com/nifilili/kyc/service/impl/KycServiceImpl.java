package com.nifilili.kyc.service.impl;

import com.nifilili.business.domain.Business;
import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;
import com.nifilili.business.events.BusinessDocumentReviewRequestedEvent;
import com.nifilili.business.events.BusinessPublishRequestedEvent;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.enums.business.DocumentStatus;
import com.nifilili.core.enums.kyc.KycStatus;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.kyc.domain.BusinessDocument;
import com.nifilili.kyc.domain.BusinessKyc;
import com.nifilili.kyc.domain.BusinessKycHistory;
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
                "Rejection reason placeholder",
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
        kyc.setSubmissionCount(
                kyc.getSubmissionCount() == null ? 1 : kyc.getSubmissionCount() + 1
        );
        kyc.setCreatedAt(Instant.now());
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
        kycRepository.save(kyc);

        saveHistory(businessId, status, request.getAdminMessage());

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        if (status == KycStatus.APPROVED) {
            business.setStatus(BusinessStatus.PUBLISHED);
        } else {
            business.setStatus(BusinessStatus.DRAFT);
        }

        businessRepository.save(business);
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
