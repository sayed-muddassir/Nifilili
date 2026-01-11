package com.nifilili.business.service.impl;

import com.nifilili.business.dto.request.SubmitBusinessForReviewRequest;
import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;
import com.nifilili.business.events.BusinessDocumentReviewRequestedEvent;
import com.nifilili.business.events.BusinessPublishRequestedEvent;
import com.nifilili.business.service.BusinessPublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusinessPublishServiceImpl implements BusinessPublishService {

    private final ApplicationEventPublisher publisher;

    @Override
    public void uploadVerificationDocuments(Long businessId, UploadBusinessDocumentRequest request) {
        publisher.publishEvent(new BusinessDocumentReviewRequestedEvent(businessId, request));
    }

    @Override
    public void submitForVerification(Long businessId, SubmitBusinessForReviewRequest request) {
        publisher.publishEvent(new BusinessPublishRequestedEvent(businessId, request.getMessage()));
    }
}
