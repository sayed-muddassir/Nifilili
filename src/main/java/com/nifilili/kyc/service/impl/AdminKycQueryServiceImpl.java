package com.nifilili.kyc.service.impl;

import com.nifilili.common.enums.KycStatus;
import com.nifilili.common.exception.ResourceNotFoundException;
import com.nifilili.kyc.domain.BusinessDocument;
import com.nifilili.kyc.domain.BusinessKyc;
import com.nifilili.kyc.dto.response.*;
import com.nifilili.kyc.repository.BusinessDocumentRepository;
import com.nifilili.kyc.repository.BusinessKycRepository;
import com.nifilili.kyc.repository.projection.AdminKycListProjection;
import com.nifilili.kyc.service.AdminKycQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminKycQueryServiceImpl
        implements AdminKycQueryService {

    private final BusinessKycRepository kycRepository;
    private final BusinessDocumentRepository documentRepository;

    @Override
    public Page<AdminKycListItemResponse> getByStatus(
            KycStatus status,
            Pageable pageable
    ) {
        return kycRepository.findByStatus(status, pageable)
                .map(this::mapListItem);
    }

    @Override
    public AdminKycDetailResponse getDetail(Long businessId) {

        BusinessKyc kyc = kycRepository.findByBusinessId(businessId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("KYC not found"));

        List<BusinessDocumentResponse> documents =
                documentRepository.findByBusinessId(businessId)
                        .stream()
                        .map(this::mapDocument)
                        .toList();

        AdminKycDetailResponse response = new AdminKycDetailResponse();
        response.setBusinessId(businessId);
        response.setBusinessName(null); // optional enrichment later
        response.setVerticalId(null);   // optional enrichment later
        response.setKycStatus(kyc.getKycStatus());
        response.setAdminMessage(kyc.getAdminMessage());
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
        r.setKycStatus(p.getKycStatus());
        r.setSubmissionCount(p.getSubmissionCount());
        r.setLastUpdatedAt(p.getUpdatedAt());
        return r;
    }

    private BusinessDocumentResponse mapDocument(
            BusinessDocument d
    ) {
        BusinessDocumentResponse r = new BusinessDocumentResponse();
        r.setId(d.getId());
        r.setDocumentDefinitionId(d.getDocumentDefinitionId());
        r.setFileName(d.getFileName());
        r.setFileUrl(d.getFileUrl());
        r.setStatus(d.getStatus());
        r.setRejectionReason(d.getRejectionReason());
        return r;
    }
}
