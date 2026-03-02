package com.nifilili.quote.service;

import com.nifilili.core.enums.quote.QuoteRequestStatus;
import com.nifilili.core.enums.quote.QuoteStatus;
import com.nifilili.quote.domain.Quote;
import com.nifilili.quote.domain.QuoteLineItem;
import com.nifilili.quote.domain.QuoteRequest;
import com.nifilili.quote.dto.request.CreateQuoteDTO;
import com.nifilili.quote.dto.request.QuoteLineItemDTO;
import com.nifilili.quote.dto.response.QuoteResponseDTO;
import com.nifilili.quote.repository.QuoteLineItemRepository;
import com.nifilili.quote.repository.QuoteRequestRepository;
import com.nifilili.quote.repository.QuoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteLineItemRepository quoteLineItemRepository;
    private final QuoteRequestRepository quoteRequestRepository;
    private final QuoteConversionService quoteConversionService;

    @Transactional
    public QuoteResponseDTO createDraft(Long requestId, CreateQuoteDTO dto) {
        QuoteRequest request = quoteRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Quote request not found"));

        if (request.getStatus() == QuoteRequestStatus.REJECTED
                || request.getStatus() == QuoteRequestStatus.CANCELLED
                || request.getStatus() == QuoteRequestStatus.EXPIRED) {
            throw new IllegalStateException("Quote request is not active");
        }

        quoteRepository.findTopByRequestIdAndStatusInOrderByIdDesc(
                requestId,
                List.of(QuoteStatus.DRAFT, QuoteStatus.SENT)
        ).ifPresent(existing -> {
            throw new IllegalStateException("An active quote already exists for this request");
        });

        Quote quote = new Quote();
        LocalDateTime now = LocalDateTime.now();
        quote.setRequestId(requestId);
        quote.setQuoteNumber(generateQuoteNumber());
        quote.setServiceDetails(dto.getServiceDetails());
        quote.setTotalAmount(dto.getTotalAmount());
        quote.setCurrency(dto.getCurrency() == null ? "INR" : dto.getCurrency());
        quote.setEstimatedDurationDays(dto.getEstimatedDurationDays());
        quote.setValidUntil(dto.getValidUntil() == null ? now.plusDays(7) : dto.getValidUntil());
        quote.setAttachments(dto.getAttachments());
        quote.setStatus(QuoteStatus.DRAFT);
        quote.setCreatedAt(now);
        quote.setUpdatedAt(now);
        quote.setCreatedBy(0L);
        quote.setUpdatedBy(0L);

        Quote savedQuote = quoteRepository.save(quote);
        saveLineItems(savedQuote.getId(), dto.getLineItems());
        return toResponse(savedQuote);
    }

    @Transactional
    public QuoteResponseDTO sendQuote(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found"));

        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new IllegalStateException("Only draft quotes can be sent");
        }

        QuoteRequest request = quoteRequestRepository.findById(quote.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Quote request not found"));

        quote.setStatus(QuoteStatus.SENT);
        quote.setSentAt(LocalDateTime.now());
        quote.setUpdatedAt(LocalDateTime.now());

        request.setStatus(QuoteRequestStatus.QUOTED);
        request.setUpdatedAt(LocalDateTime.now());
        quoteRequestRepository.save(request);

        return toResponse(quoteRepository.save(quote));
    }

    @Transactional
    public QuoteResponseDTO reviseQuote(Long oldQuoteId, CreateQuoteDTO dto) {
        Quote oldQuote = quoteRepository.findById(oldQuoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found"));

        if (oldQuote.getStatus() != QuoteStatus.SENT) {
            throw new IllegalStateException("Only sent quotes can be revised");
        }

        oldQuote.setStatus(QuoteStatus.REVISED);
        oldQuote.setUpdatedAt(LocalDateTime.now());
        quoteRepository.save(oldQuote);

        Quote revised = new Quote();
        LocalDateTime now = LocalDateTime.now();
        revised.setRequestId(oldQuote.getRequestId());
        revised.setParentQuoteId(oldQuote.getId());
        revised.setQuoteNumber(generateQuoteNumber());
        revised.setServiceDetails(dto.getServiceDetails());
        revised.setTotalAmount(dto.getTotalAmount());
        revised.setCurrency(dto.getCurrency() == null ? oldQuote.getCurrency() : dto.getCurrency());
        revised.setEstimatedDurationDays(dto.getEstimatedDurationDays());
        revised.setValidUntil(dto.getValidUntil() == null ? now.plusDays(7) : dto.getValidUntil());
        revised.setAttachments(dto.getAttachments());
        revised.setStatus(QuoteStatus.DRAFT);
        revised.setCreatedAt(now);
        revised.setUpdatedAt(now);
        revised.setCreatedBy(0L);
        revised.setUpdatedBy(0L);

        Quote saved = quoteRepository.save(revised);
        saveLineItems(saved.getId(), dto.getLineItems());
        return toResponse(saved);
    }

    @Transactional
    public QuoteResponseDTO acceptQuote(Long quoteId, Long userId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found"));
        QuoteRequest request = quoteRequestRepository.findById(quote.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Quote request not found"));

        if (!request.getUserId().equals(userId)) {
            throw new IllegalStateException("Unauthorized to accept this quote");
        }
        if (quote.getStatus() != QuoteStatus.SENT) {
            throw new IllegalStateException("Only sent quotes can be accepted");
        }
        if (quote.getValidUntil().isBefore(LocalDateTime.now())) {
            quote.setStatus(QuoteStatus.EXPIRED);
            quote.setUpdatedAt(LocalDateTime.now());
            quoteRepository.save(quote);
            throw new IllegalStateException("Quote is expired");
        }

        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setAcceptedAt(LocalDateTime.now());
        quote.setUpdatedAt(LocalDateTime.now());

        request.setStatus(QuoteRequestStatus.ACCEPTED);
        request.setUpdatedAt(LocalDateTime.now());

        quoteRequestRepository.save(request);
        Quote saved = quoteRepository.save(quote);
        quoteConversionService.convertToOrder(saved.getId(), userId);
        return toResponse(saved);
    }

    @Transactional
    public QuoteResponseDTO rejectQuote(Long quoteId, Long userId, String reason) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found"));
        QuoteRequest request = quoteRequestRepository.findById(quote.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Quote request not found"));

        if (!request.getUserId().equals(userId)) {
            throw new IllegalStateException("Unauthorized to reject this quote");
        }
        if (quote.getStatus() != QuoteStatus.SENT) {
            throw new IllegalStateException("Only sent quotes can be rejected");
        }

        quote.setStatus(QuoteStatus.REJECTED);
        quote.setRejectedAt(LocalDateTime.now());
        quote.setRejectionReason(reason);
        quote.setUpdatedAt(LocalDateTime.now());

        request.setStatus(QuoteRequestStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setUpdatedAt(LocalDateTime.now());

        quoteRequestRepository.save(request);
        return toResponse(quoteRepository.save(quote));
    }

    public List<QuoteResponseDTO> listQuotesForRequest(Long requestId) {
        return quoteRepository.findByRequestIdOrderByIdDesc(requestId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public QuoteResponseDTO getLatestQuote(Long requestId) {
        Quote quote = quoteRepository.findTopByRequestIdOrderByIdDesc(requestId)
                .orElseThrow(() -> new IllegalArgumentException("No quote found for request"));
        return toResponse(quote);
    }

    @Transactional
    public void expireQuotes() {
        LocalDateTime now = LocalDateTime.now();
        List<Quote> expiring = quoteRepository.findByStatusAndValidUntilBefore(QuoteStatus.SENT, now);
        for (Quote quote : expiring) {
            quote.setStatus(QuoteStatus.EXPIRED);
            quote.setUpdatedAt(now);
        }
        quoteRepository.saveAll(expiring);
    }

    private void saveLineItems(Long quoteId, List<QuoteLineItemDTO> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("At least one quote line item is required");
        }

        List<QuoteLineItem> entities = lineItems.stream()
                .map(dto -> {
                    if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
                        throw new IllegalArgumentException("Line item quantity must be greater than 0");
                    }
                    if (dto.getUnitPrice() == null || dto.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("Line item unit price must be >= 0");
                    }
                    QuoteLineItem item = new QuoteLineItem();
                    item.setQuoteId(quoteId);
                    item.setDescription(dto.getDescription());
                    item.setQuantity(dto.getQuantity());
                    item.setUnitPrice(dto.getUnitPrice());
                    item.setTotalPrice(dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
                    item.setCreatedAt(LocalDateTime.now());
                    item.setUpdatedAt(LocalDateTime.now());
                    item.setCreatedBy(0L);
                    item.setUpdatedBy(0L);
                    return item;
                })
                .toList();

        quoteLineItemRepository.saveAll(entities);
    }

    private QuoteResponseDTO toResponse(Quote quote) {
        QuoteResponseDTO response = new QuoteResponseDTO();
        response.setId(quote.getId());
        response.setQuoteNumber(quote.getQuoteNumber());
        response.setRequestId(quote.getRequestId());
        response.setServiceDetails(quote.getServiceDetails());
        response.setTotalAmount(quote.getTotalAmount());
        response.setCurrency(quote.getCurrency());
        response.setEstimatedDurationDays(quote.getEstimatedDurationDays());
        response.setValidUntil(quote.getValidUntil());
        response.setStatus(quote.getStatus());
        response.setSentAt(quote.getSentAt());
        response.setAcceptedAt(quote.getAcceptedAt());
        response.setRejectedAt(quote.getRejectedAt());
        response.setRejectionReason(quote.getRejectionReason());
        response.setLineItems(quoteLineItemRepository.findByQuoteId(quote.getId())
                .stream()
                .map(this::toLineItemDto)
                .toList());
        return response;
    }

    private QuoteLineItemDTO toLineItemDto(QuoteLineItem lineItem) {
        QuoteLineItemDTO dto = new QuoteLineItemDTO();
        dto.setDescription(lineItem.getDescription());
        dto.setQuantity(lineItem.getQuantity());
        dto.setUnitPrice(lineItem.getUnitPrice());
        return dto;
    }

    private String generateQuoteNumber() {
        return "QTE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
