package com.nifilili.business.service.impl;

import com.nifilili.business.domain.Business;
import com.nifilili.business.dto.request.SubmitBusinessForReviewRequest;
import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;
import com.nifilili.business.events.BusinessClaimedEvent;
import com.nifilili.business.events.BusinessDocumentReviewRequestedEvent;
import com.nifilili.business.events.BusinessPublishRequestedEvent;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.business.service.BusinessPublishService;
import com.nifilili.core.enums.business.BusinessSource;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessPublishServiceImpl implements BusinessPublishService {

    private final ApplicationEventPublisher publisher;
    private final BusinessRepository businessRepository;

    @Override
    public void uploadVerificationDocuments(Long businessId, UploadBusinessDocumentRequest request) {
        ensureCurrentUserOwnsBusiness(businessId);
        publisher.publishEvent(new BusinessDocumentReviewRequestedEvent(businessId, request));
    }

    @Override
    public void submitForVerification(Long businessId, SubmitBusinessForReviewRequest request) {
        ensureCurrentUserOwnsBusiness(businessId);
        publisher.publishEvent(new BusinessPublishRequestedEvent(businessId, request.getMessage()));
    }

    @Override
    @Transactional
    // After claiming the business, User needs to upload KYC documents and submit for verification.
    public void claimBusiness(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        if (business.getSource() != BusinessSource.ADMIN_SEEDED) {
            throw new InvalidBusinessStateException("Only admin-seeded businesses can be claimed");
        }
        if (business.getStatus() == BusinessStatus.CLAIM_UNDER_PROGRESS) {
            throw new InvalidBusinessStateException("Cannot claim a business that is already under claim");
        }
        if (business.isClaimed()) {
            throw new InvalidBusinessStateException("Business is already claimed");
        }

        Long userId = SecurityUtil.getCurrentUserId();

        business.setClaimedByUserId(userId);
        business.setOwnerUserId(userId);
        business.setStatus(BusinessStatus.CLAIM_UNDER_PROGRESS);
        businessRepository.save(business);

        // This event is consumed by KYC module, will initiate KYC record with NOT STARTED, upload document and submit for review will move it to PENDING
        // Based KYC ADMIN Approval or Rejection will move the business to PUBLISHED or DRAFT
        publisher.publishEvent(new BusinessClaimedEvent(businessId, userId));
        log.info("Business id='{}' claimed by user='{}'", businessId, userId);
    }

    private void ensureCurrentUserOwnsBusiness(Long businessId) {
        var business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        Long authenticatedUserId = SecurityUtil.getCurrentUserId();
        if (business.getOwnerUserId() == null || !business.getOwnerUserId().equals(authenticatedUserId)) {
            throw new InvalidBusinessStateException("Unauthorized access, logged in user does not owns this business");
        }
    }
}
