package com.nifilili.quote.service.impl;

import com.nifilili.core.enums.quote.QuoteRequestStatus;
import com.nifilili.core.exception.InvalidQuoteStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.quote.domain.QuoteRequestEntity;
import com.nifilili.quote.dto.request.CreateQuoteRequestRequest;
import com.nifilili.quote.dto.response.QuoteRequestResponse;
import com.nifilili.quote.events.QuoteExpiredEvent;
import com.nifilili.quote.events.QuoteRequestCreatedEvent;
import com.nifilili.quote.events.QuoteRequestDeclinedEvent;
import com.nifilili.quote.mapper.QuoteRequestMapper;
import com.nifilili.quote.repository.QuoteRequestRepository;
import com.nifilili.quote.service.QuoteRequestService;
import com.nifilili.quote.validation.QuoteValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteRequestServiceImpl implements QuoteRequestService {

    private final QuoteRequestRepository quoteRequestRepository;
    private final QuoteRequestMapper quoteRequestMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public QuoteRequestResponse createRequest(CreateQuoteRequestRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Creating quote request for userId={}, offeringId={}", userId, request.getOfferingId());

        LocalDateTime now = LocalDateTime.now();
        String requestNumber = "QREQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Map<String, Object> deliveryAddress = null;
        if (request.getDeliveryAddress() != null) {
            deliveryAddress = new HashMap<>();
            deliveryAddress.put("municipalityId", request.getDeliveryAddress().getMunicipalityId());
            deliveryAddress.put("wardNumber", request.getDeliveryAddress().getWardNumber());
            deliveryAddress.put("toleName", request.getDeliveryAddress().getToleName());
            deliveryAddress.put("addressField1", request.getDeliveryAddress().getAddressField1());
            deliveryAddress.put("postalCode", request.getDeliveryAddress().getPostalCode());
        }

        QuoteRequestEntity entity = QuoteRequestEntity.builder()
                .offeringId(request.getOfferingId())
                .userId(userId)
                .businessId(request.getBusinessId())
                .requestNumber(requestNumber)
                .requirements(request.getRequirements())
                .budgetRange(request.getBudgetRange())
                .preferredTimeline(request.getPreferredTimeline())
                .deliveryAddress(deliveryAddress)
                .attachments(request.getAttachments())
                .status(QuoteRequestStatus.PENDING)
                .submittedAt(now)
                .expiredAt(now.plusDays(30))
                .createdAt(now)
                .updatedAt(now)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        entity = quoteRequestRepository.save(entity);
        log.info("Quote request created: requestId={}, requestNumber={}", entity.getId(), requestNumber);

        eventPublisher.publishEvent(new QuoteRequestCreatedEvent(
                entity.getId(), entity.getOfferingId(), userId, entity.getBusinessId(), requestNumber));

        return quoteRequestMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public QuoteRequestResponse getRequest(Long requestId) {
        log.debug("Getting quote request requestId={}", requestId);
        QuoteRequestEntity entity = findRequestOrThrow(requestId);
        return quoteRequestMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuoteRequestResponse> listMyRequests() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Listing quote requests for userId={}", userId);
        return quoteRequestRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(quoteRequestMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuoteRequestResponse> listBusinessRequests(Long businessId) {
        log.debug("Listing quote requests for businessId={}", businessId);
        return quoteRequestRepository.findByBusinessIdOrderByIdDesc(businessId).stream()
                .map(quoteRequestMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public QuoteRequestResponse declineRequest(Long requestId, String reason) {
        log.info("Declining quote request requestId={}", requestId);
        QuoteRequestEntity entity = findRequestOrThrow(requestId);
        QuoteValidator.ensureRequestStatus(entity, QuoteRequestStatus.PENDING);

        LocalDateTime now = LocalDateTime.now();
        entity.setStatus(QuoteRequestStatus.REJECTED);
        entity.setRejectionReason(reason);
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(SecurityUtil.getCurrentUserId());
        entity = quoteRequestRepository.save(entity);

        log.info("Quote request declined: requestId={}", requestId);
        eventPublisher.publishEvent(new QuoteRequestDeclinedEvent(
                requestId, entity.getUserId(), entity.getBusinessId(), reason));

        return quoteRequestMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public QuoteRequestResponse cancelRequest(Long requestId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Cancelling quote request requestId={} by userId={}", requestId, userId);

        QuoteRequestEntity entity = findRequestOrThrow(requestId);
        if (!entity.getUserId().equals(userId)) {
            throw new InvalidQuoteStateException("Only the request owner can cancel a quote request");
        }
        QuoteValidator.ensureRequestStatus(entity, QuoteRequestStatus.PENDING);

        LocalDateTime now = LocalDateTime.now();
        entity.setStatus(QuoteRequestStatus.CANCELLED);
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(userId);
        entity = quoteRequestRepository.save(entity);

        log.info("Quote request cancelled: requestId={}", requestId);
        return quoteRequestMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void expireRequests() {
        LocalDateTime now = LocalDateTime.now();
        List<QuoteRequestEntity> expired = quoteRequestRepository
                .findByStatusAndExpiredAtBefore(QuoteRequestStatus.PENDING, now);

        if (expired.isEmpty()) {
            return;
        }

        log.info("Expiring {} quote requests", expired.size());
        for (QuoteRequestEntity entity : expired) {
            entity.setStatus(QuoteRequestStatus.EXPIRED);
            entity.setUpdatedAt(now);
            entity.setUpdatedBy(entity.getCreatedBy());
            quoteRequestRepository.save(entity);

            eventPublisher.publishEvent(new QuoteExpiredEvent(
                    null, entity.getId(), entity.getUserId(), entity.getBusinessId()));
        }
    }

    private QuoteRequestEntity findRequestOrThrow(Long requestId) {
        return quoteRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote request not found: " + requestId));
    }
}
