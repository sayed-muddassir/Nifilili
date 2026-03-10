package com.nifilili.kyc.service.impl;

import com.nifilili.business.api.BusinessQueryApi;
import com.nifilili.business.api.model.BusinessMetaData;
import com.nifilili.core.enums.kyc.KycStatus;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.kyc.domain.BusinessKyc;
import com.nifilili.kyc.dto.response.AdminKycDetailResponse;
import com.nifilili.kyc.dto.response.AdminKycListItemResponse;
import com.nifilili.kyc.dto.response.BusinessDocumentResponse;
import com.nifilili.kyc.mapper.BusinessDocumentMapper;
import com.nifilili.kyc.repository.BusinessDocumentRepository;
import com.nifilili.kyc.repository.BusinessKycRepository;
import com.nifilili.kyc.repository.projection.AdminKycListProjection;
import com.nifilili.kyc.service.AdminKycQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminKycQueryServiceImpl
        implements AdminKycQueryService {

    private final BusinessKycRepository kycRepository;
    private final BusinessDocumentRepository documentRepository;
    private final BusinessDocumentMapper businessDocumentMapper;
    private final BusinessQueryApi businessQueryApi;

    @Override
    public Page<AdminKycListItemResponse> getByStatus(
            KycStatus status,
            Pageable pageable
    ) {
        log.debug("Fetching KYC list with status={}", status);
        return kycRepository.findByStatus(status.name(), pageable)
                .map(this::mapListItem);
    }

    @Override
    public AdminKycDetailResponse getDetail(Long businessId) {
        log.debug("Fetching KYC detail for businessId={}", businessId);

        BusinessKyc kyc = kycRepository.findByBusinessId(businessId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("KYC not found for businessId: " + businessId));

        List<BusinessDocumentResponse> documents =
                documentRepository.findByBusinessId(businessId)
                        .stream()
                        .map(businessDocumentMapper::toResponse)
                        .toList();
        BusinessMetaData metaData = businessQueryApi.getBusinessDetailsById(businessId);

        return getAdminKycDetailResponse(businessId, kyc, documents, metaData);
    }

    private static @NonNull AdminKycDetailResponse getAdminKycDetailResponse(Long businessId, BusinessKyc kyc, List<BusinessDocumentResponse> documents, BusinessMetaData businessMetaData) {
        AdminKycDetailResponse response = new AdminKycDetailResponse();
        response.setBusinessId(businessId);
        response.setBusinessName(businessMetaData.getBusinessName()); // optional enrichment later
        response.setVerticalId(businessMetaData.getVerticalId());   // optional enrichment later
        response.setKycStatus(kyc.getKycStatus());
        response.setAdminMessage(kyc.getAdminMessage());
        response.setRejectionReason(kyc.getRejectionReason());
        response.setSubmissionCount(kyc.getSubmissionCount());
        response.setDocuments(documents);
        return response;
    }

    /* ------------------ MAPPERS ------------------ */

    private AdminKycListItemResponse mapListItem(
            AdminKycListProjection p
    ) {
        AdminKycListItemResponse r = new AdminKycListItemResponse();
        r.setBusinessId(p.getBusinessId());
        r.setBusinessName(p.getBusinessName());
        r.setVerticalId(p.getVerticalId());
        r.setKycStatus(p.getKycStatus() != null ? KycStatus.valueOf(p.getKycStatus()) : null);
        r.setSubmissionCount(p.getSubmissionCount());
        r.setLastUpdatedAt(p.getUpdatedAt());
        return r;
    }
}
