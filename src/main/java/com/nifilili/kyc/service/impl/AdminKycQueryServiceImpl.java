package com.nifilili.kyc.service.impl;

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
    private final BusinessDocumentMapper businessDocumentMapper;

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
                        .map(businessDocumentMapper::toResponse)
                        .toList();

        AdminKycDetailResponse response = new AdminKycDetailResponse();
        response.setBusinessId(businessId);
        response.setBusinessName(null); // optional enrichment later
        response.setVerticalId(null);   // optional enrichment later
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
        r.setKycStatus(p.getKycStatus());
        r.setSubmissionCount(p.getSubmissionCount());
        r.setLastUpdatedAt(p.getUpdatedAt());
        return r;
    }
}
