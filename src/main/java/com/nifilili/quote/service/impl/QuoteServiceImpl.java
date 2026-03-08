package com.nifilili.quote.service.impl;

import com.nifilili.core.enums.quote.QuoteRequestStatus;
import com.nifilili.core.enums.quote.QuoteStatus;
import com.nifilili.core.exception.InvalidQuoteStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.quote.domain.QuoteEntity;
import com.nifilili.quote.domain.QuoteLineItemEntity;
import com.nifilili.quote.domain.QuoteRequestEntity;
import com.nifilili.quote.dto.request.CreateQuoteRequest;
import com.nifilili.quote.dto.request.QuoteLineItemRequest;
import com.nifilili.quote.dto.request.RejectQuoteRequest;
import com.nifilili.quote.dto.response.QuoteResponse;
import com.nifilili.quote.events.QuoteAcceptedEvent;
import com.nifilili.quote.events.QuoteExpiredEvent;
import com.nifilili.quote.events.QuoteRejectedEvent;
import com.nifilili.quote.events.QuoteSentEvent;
import com.nifilili.quote.mapper.QuoteMapper;
import com.nifilili.quote.repository.QuoteLineItemRepository;
import com.nifilili.quote.repository.QuoteRepository;
import com.nifilili.quote.repository.QuoteRequestRepository;
import com.nifilili.quote.service.QuoteService;
import com.nifilili.quote.validation.QuoteValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteRequestRepository quoteRequestRepository;
    private final QuoteLineItemRepository quoteLineItemRepository;
    private final QuoteMapper quoteMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public QuoteResponse createDraft(Long requestId, CreateQuoteRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Creating draft quote for requestId={}", requestId);

        QuoteRequestEntity quoteRequest = findRequestOrThrow(requestId);
        QuoteValidator.ensureRequestNotTerminal(quoteRequest);

        boolean hasActiveQuote = quoteRepository.existsByRequestIdAndStatusIn(
                requestId, List.of(QuoteStatus.DRAFT, QuoteStatus.SENT));
        if (hasActiveQuote) {
            throw new InvalidQuoteStateException(
                    "An active quote (DRAFT or SENT) already exists for request " + requestId);
        }

        LocalDateTime now = LocalDateTime.now();
        String quoteNumber = "QTE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        BigDecimal totalAmount = calculateTotal(request.getLineItems());

        QuoteEntity quote = QuoteEntity.builder()
                .requestId(requestId)
                .quoteNumber(quoteNumber)
                .serviceDetails(request.getServiceDetails())
                .totalAmount(totalAmount)
                .currency(request.getCurrency())
                .estimatedDurationDays(request.getEstimatedDurationDays())
                .validUntil(now.plusDays(request.getValidityDays()))
                .attachments(request.getAttachments())
                .status(QuoteStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        quote = quoteRepository.save(quote);
        saveLineItems(quote.getId(), request.getLineItems(), userId, now);

        log.info("Draft quote created: quoteId={}, quoteNumber={}", quote.getId(), quoteNumber);
        return buildQuoteResponse(quote);
    }

    @Override
    @Transactional
    public QuoteResponse sendQuote(Long quoteId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Sending quote quoteId={}", quoteId);

        QuoteEntity quote = findQuoteOrThrow(quoteId);
        QuoteValidator.ensureQuoteStatus(quote, QuoteStatus.DRAFT);

        LocalDateTime now = LocalDateTime.now();
        quote.setStatus(QuoteStatus.SENT);
        quote.setSentAt(now);
        quote.setUpdatedAt(now);
        quote.setUpdatedBy(userId);
        quote = quoteRepository.save(quote);

        // Update request status to QUOTED
        QuoteRequestEntity request = findRequestOrThrow(quote.getRequestId());
        if (request.getStatus() == QuoteRequestStatus.PENDING) {
            request.setStatus(QuoteRequestStatus.QUOTED);
            request.setUpdatedAt(now);
            request.setUpdatedBy(userId);
            quoteRequestRepository.save(request);
        }

        log.info("Quote sent: quoteId={}, quoteNumber={}", quoteId, quote.getQuoteNumber());
        eventPublisher.publishEvent(new QuoteSentEvent(
                quoteId, quote.getRequestId(), request.getUserId(),
                request.getBusinessId(), quote.getQuoteNumber(), quote.getTotalAmount()));

        return buildQuoteResponse(quote);
    }

    @Override
    @Transactional
    public QuoteResponse reviseQuote(Long quoteId, CreateQuoteRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Revising quote quoteId={}", quoteId);

        QuoteEntity oldQuote = findQuoteOrThrow(quoteId);
        QuoteValidator.ensureQuoteStatus(oldQuote, QuoteStatus.SENT);

        LocalDateTime now = LocalDateTime.now();

        // Mark old quote as REVISED
        oldQuote.setStatus(QuoteStatus.REVISED);
        oldQuote.setUpdatedAt(now);
        oldQuote.setUpdatedBy(userId);
        quoteRepository.save(oldQuote);

        // Create new draft linked to old
        String quoteNumber = "QTE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BigDecimal totalAmount = calculateTotal(request.getLineItems());

        QuoteEntity newQuote = QuoteEntity.builder()
                .requestId(oldQuote.getRequestId())
                .parentQuoteId(quoteId)
                .quoteNumber(quoteNumber)
                .serviceDetails(request.getServiceDetails())
                .totalAmount(totalAmount)
                .currency(request.getCurrency())
                .estimatedDurationDays(request.getEstimatedDurationDays())
                .validUntil(now.plusDays(request.getValidityDays()))
                .attachments(request.getAttachments())
                .status(QuoteStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        newQuote = quoteRepository.save(newQuote);
        saveLineItems(newQuote.getId(), request.getLineItems(), userId, now);

        log.info("Quote revised: oldQuoteId={}, newQuoteId={}, newQuoteNumber={}",
                quoteId, newQuote.getId(), quoteNumber);
        return buildQuoteResponse(newQuote);
    }

    @Override
    @Transactional
    public QuoteResponse acceptQuote(Long quoteId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Accepting quote quoteId={} by userId={}", quoteId, userId);

        QuoteEntity quote = findQuoteOrThrow(quoteId);
        QuoteValidator.ensureQuoteStatus(quote, QuoteStatus.SENT);
        QuoteValidator.ensureQuoteNotExpired(quote);

        QuoteRequestEntity request = findRequestOrThrow(quote.getRequestId());
        if (!request.getUserId().equals(userId)) {
            throw new InvalidQuoteStateException("Only the quote request owner can accept a quote");
        }

        LocalDateTime now = LocalDateTime.now();

        // Update quote
        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setAcceptedAt(now);
        quote.setUpdatedAt(now);
        quote.setUpdatedBy(userId);
        quote = quoteRepository.save(quote);

        // Update request
        request.setStatus(QuoteRequestStatus.ACCEPTED);
        request.setUpdatedAt(now);
        request.setUpdatedBy(userId);
        quoteRequestRepository.save(request);

        // Publish event with all data needed for order creation
        List<QuoteLineItemEntity> lineItems = quoteLineItemRepository.findByQuoteIdOrderByIdAsc(quoteId);
        List<QuoteAcceptedEvent.LineItemData> lineItemData = lineItems.stream()
                .map(li -> new QuoteAcceptedEvent.LineItemData(
                        li.getDescription(), li.getQuantity(), li.getUnitPrice(), li.getTotalPrice()))
                .toList();

        eventPublisher.publishEvent(new QuoteAcceptedEvent(
                quoteId, quote.getRequestId(), userId, request.getBusinessId(),
                request.getOfferingId(), quote.getQuoteNumber(), quote.getServiceDetails(),
                quote.getTotalAmount(), quote.getCurrency(), request.getDeliveryAddress(), lineItemData));

        log.info("Quote accepted: quoteId={}, quoteNumber={}", quoteId, quote.getQuoteNumber());
        return buildQuoteResponse(quote);
    }

    @Override
    @Transactional
    public QuoteResponse rejectQuote(Long quoteId, RejectQuoteRequest rejectRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Rejecting quote quoteId={} by userId={}", quoteId, userId);

        QuoteEntity quote = findQuoteOrThrow(quoteId);
        QuoteValidator.ensureQuoteStatus(quote, QuoteStatus.SENT);

        QuoteRequestEntity request = findRequestOrThrow(quote.getRequestId());
        if (!request.getUserId().equals(userId)) {
            throw new InvalidQuoteStateException("Only the quote request owner can reject a quote");
        }

        LocalDateTime now = LocalDateTime.now();
        String reason = rejectRequest != null ? rejectRequest.getReason() : null;

        // Update quote
        quote.setStatus(QuoteStatus.REJECTED);
        quote.setRejectedAt(now);
        quote.setRejectionReason(reason);
        quote.setUpdatedAt(now);
        quote.setUpdatedBy(userId);
        quote = quoteRepository.save(quote);

        // Update request
        request.setStatus(QuoteRequestStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setUpdatedAt(now);
        request.setUpdatedBy(userId);
        quoteRequestRepository.save(request);

        log.info("Quote rejected: quoteId={}, quoteNumber={}", quoteId, quote.getQuoteNumber());
        eventPublisher.publishEvent(new QuoteRejectedEvent(
                quoteId, quote.getRequestId(), userId, request.getBusinessId(), reason));

        return buildQuoteResponse(quote);
    }

    @Override
    @Transactional(readOnly = true)
    public QuoteResponse getQuote(Long quoteId) {
        log.debug("Getting quote quoteId={}", quoteId);
        QuoteEntity quote = findQuoteOrThrow(quoteId);
        return buildQuoteResponse(quote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuoteResponse> listQuotesForRequest(Long requestId) {
        log.debug("Listing quotes for requestId={}", requestId);
        return quoteRepository.findByRequestIdOrderByIdDesc(requestId).stream()
                .map(this::buildQuoteResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuoteResponse getLatestQuote(Long requestId) {
        log.debug("Getting latest quote for requestId={}", requestId);
        QuoteEntity quote = quoteRepository.findFirstByRequestIdOrderByIdDesc(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("No quotes found for request " + requestId));
        return buildQuoteResponse(quote);
    }

    @Override
    @Transactional
    public void expireQuotes() {
        LocalDateTime now = LocalDateTime.now();
        List<QuoteEntity> expired = quoteRepository.findByStatusAndValidUntilBefore(QuoteStatus.SENT, now);

        if (expired.isEmpty()) {
            return;
        }

        log.info("Expiring {} quotes", expired.size());
        for (QuoteEntity quote : expired) {
            quote.setStatus(QuoteStatus.EXPIRED);
            quote.setUpdatedAt(now);
            quote.setUpdatedBy(quote.getCreatedBy());
            quoteRepository.save(quote);

            QuoteRequestEntity request = quoteRequestRepository.findById(quote.getRequestId()).orElse(null);
            Long userId = request != null ? request.getUserId() : null;
            Long businessId = request != null ? request.getBusinessId() : null;

            eventPublisher.publishEvent(new QuoteExpiredEvent(
                    quote.getId(), quote.getRequestId(), userId, businessId));
        }
    }

    private QuoteEntity findQuoteOrThrow(Long quoteId) {
        return quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found: " + quoteId));
    }

    private QuoteRequestEntity findRequestOrThrow(Long requestId) {
        return quoteRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote request not found: " + requestId));
    }

    private BigDecimal calculateTotal(List<QuoteLineItemRequest> lineItems) {
        return lineItems.stream()
                .map(li -> li.getUnitPrice().multiply(BigDecimal.valueOf(li.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void saveLineItems(Long quoteId, List<QuoteLineItemRequest> lineItems, Long userId, LocalDateTime now) {
        for (QuoteLineItemRequest li : lineItems) {
            BigDecimal totalPrice = li.getUnitPrice().multiply(BigDecimal.valueOf(li.getQuantity()));
            QuoteLineItemEntity entity = QuoteLineItemEntity.builder()
                    .quoteId(quoteId)
                    .description(li.getDescription())
                    .quantity(li.getQuantity())
                    .unitPrice(li.getUnitPrice())
                    .totalPrice(totalPrice)
                    .createdAt(now)
                    .updatedAt(now)
                    .createdBy(userId)
                    .updatedBy(userId)
                    .build();
            quoteLineItemRepository.save(entity);
        }
    }

    private QuoteResponse buildQuoteResponse(QuoteEntity quote) {
        QuoteResponse response = quoteMapper.toResponse(quote);
        List<QuoteLineItemEntity> lineItems = quoteLineItemRepository.findByQuoteIdOrderByIdAsc(quote.getId());
        response.setLineItems(lineItems.stream().map(quoteMapper::toLineItemResponse).toList());
        return response;
    }
}
