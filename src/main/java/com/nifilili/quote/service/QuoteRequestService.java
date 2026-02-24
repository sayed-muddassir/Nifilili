package com.nifilili.quote.service;

import com.nifilili.core.enums.offering.OfferingType;
import com.nifilili.core.enums.quote.QuoteRequestStatus;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.quote.domain.QuoteRequest;
import com.nifilili.quote.dto.request.CreateQuoteRequestDTO;
import com.nifilili.quote.dto.response.QuoteRequestResponseDTO;
import com.nifilili.quote.repository.QuoteRequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteRequestService {

    private final QuoteRequestRepository quoteRequestRepository;
    private final OfferingRepository offeringRepository;

    @Transactional
    public QuoteRequestResponseDTO createRequest(CreateQuoteRequestDTO dto) {
        OfferingEntity offering = validateDynamicServiceOffering(dto);
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        QuoteRequest request = new QuoteRequest();
        request.setOfferingId(dto.getOfferingId());
        request.setBusinessId(dto.getBusinessId());
        request.setUserId(userId);
        request.setRequestNumber(generateRequestNumber());
        request.setRequirements(dto.getRequirements());
        request.setBudgetRange(dto.getBudgetRange());
        request.setPreferredTimeline(dto.getPreferredTimeline());
        request.setDeliveryAddress(toAddressMap(dto));
        request.setAttachments(dto.getAttachments());
        request.setStatus(QuoteRequestStatus.PENDING);
        request.setSubmittedAt(now);
        request.setExpiredAt(now.plusDays(30));
        request.setCreatedAt(now);
        request.setUpdatedAt(now);
        request.setCreatedBy(userId);
        request.setUpdatedBy(userId);

        return toResponse(quoteRequestRepository.save(request));
    }

    public List<QuoteRequestResponseDTO> listMyRequests() {
        Long userId = SecurityUtil.getCurrentUserId();
        return quoteRequestRepository.findByUserIdOrderByIdDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<QuoteRequestResponseDTO> listBusinessRequests(Long businessId) {
        return quoteRequestRepository.findByBusinessIdOrderByIdDesc(businessId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public QuoteRequestResponseDTO declineRequest(Long requestId, String reason) {
        QuoteRequest request = quoteRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Quote request not found"));

        if (request.getStatus() != QuoteRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending quote requests can be declined");
        }

        request.setStatus(QuoteRequestStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setUpdatedAt(LocalDateTime.now());
        request.setUpdatedBy(SecurityUtil.getCurrentUserId());

        return toResponse(quoteRequestRepository.save(request));
    }

    @Transactional
    public void expireRequests() {
        LocalDateTime now = LocalDateTime.now();
        List<QuoteRequest> expiring =
                quoteRequestRepository.findByStatusAndExpiredAtBefore(QuoteRequestStatus.PENDING, now);

        for (QuoteRequest request : expiring) {
            request.setStatus(QuoteRequestStatus.EXPIRED);
            request.setUpdatedAt(now);
        }
        quoteRequestRepository.saveAll(expiring);
    }

    private OfferingEntity validateDynamicServiceOffering(CreateQuoteRequestDTO dto) {
        OfferingEntity offering = offeringRepository.findById(dto.getOfferingId())
                .orElseThrow(() -> new IllegalArgumentException("Offering not found"));

        if (!offering.getOwnerId().equals(dto.getBusinessId())) {
            throw new IllegalArgumentException("Business does not own this offering");
        }
        if (offering.getType() != OfferingType.SERVICE) {
            throw new IllegalArgumentException("Quote requests are allowed only for service offerings");
        }
        if (!Boolean.TRUE.equals(offering.getIsDynamicPricing())) {
            throw new IllegalArgumentException("Quote requests are allowed only for dynamic pricing services");
        }

        return offering;
    }

    private String generateRequestNumber() {
        return "QREQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Map<String, Object> toAddressMap(CreateQuoteRequestDTO dto) {
        if (dto.getDeliveryAddress() == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("municipalityId", dto.getDeliveryAddress().getMunicipalityId());
        map.put("wardNumber", dto.getDeliveryAddress().getWardNumber());
        map.put("toleName", dto.getDeliveryAddress().getToleName());
        map.put("addressField1", dto.getDeliveryAddress().getAddressField1());
        map.put("postalCode", dto.getDeliveryAddress().getPostalCode());
        return map;
    }

    private QuoteRequestResponseDTO toResponse(QuoteRequest request) {
        QuoteRequestResponseDTO response = new QuoteRequestResponseDTO();
        response.setId(request.getId());
        response.setRequestNumber(request.getRequestNumber());
        response.setOfferingId(request.getOfferingId());
        response.setBusinessId(request.getBusinessId());
        response.setRequirements(request.getRequirements());
        response.setBudgetRange(request.getBudgetRange());
        response.setPreferredTimeline(request.getPreferredTimeline());
        response.setStatus(request.getStatus());
        response.setSubmittedAt(request.getSubmittedAt());
        response.setExpiredAt(request.getExpiredAt());
        return response;
    }
}
