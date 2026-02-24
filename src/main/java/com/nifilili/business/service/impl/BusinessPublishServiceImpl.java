package com.nifilili.business.service.impl;

import com.nifilili.business.dto.request.SubmitBusinessForReviewRequest;
import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;
import com.nifilili.business.events.BusinessDocumentReviewRequestedEvent;
import com.nifilili.business.events.BusinessPublishRequestedEvent;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.business.service.BusinessPublishService;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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

    private void ensureCurrentUserOwnsBusiness(Long businessId) {
        var business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        Long authenticatedUserId = SecurityUtil.getCurrentUserId();
        if (business.getOwnerUserId() == null || !business.getOwnerUserId().equals(authenticatedUserId)) {
            throw new InvalidBusinessStateException("Unauthorized access");
        }
    }
}
