package com.nifilili.kyc.service.impl;

import com.nifilili.business.api.BusinessQueryApi;
import com.nifilili.business.api.model.BusinessMetaData;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.kyc.domain.BusinessKyc;
import com.nifilili.kyc.dto.response.BusinessDocumentResponse;
import com.nifilili.kyc.dto.response.KycStatusResponse;
import com.nifilili.kyc.mapper.BusinessDocumentMapper;
import com.nifilili.kyc.repository.BusinessDocumentRepository;
import com.nifilili.kyc.repository.BusinessKycRepository;
import com.nifilili.kyc.service.OwnerKycQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OwnerKycQueryServiceImpl implements OwnerKycQueryService {

    private final BusinessKycRepository kycRepository;
    private final BusinessDocumentRepository documentRepository;
    private final BusinessDocumentMapper businessDocumentMapper;
    private final BusinessQueryApi businessQueryApi;

    @Override
    public KycStatusResponse getStatus(Long businessId) {
        ensureCurrentUserOwnsBusiness(businessId);

        BusinessKyc kyc = kycRepository.findByBusinessId(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC not found for businessId: " + businessId));

        log.debug("Returning KYC status for businessId={}, status={}", businessId, kyc.getKycStatus());

        KycStatusResponse response = new KycStatusResponse();
        response.setBusinessId(businessId);
        response.setKycStatus(kyc.getKycStatus());
        response.setRejectionReason(kyc.getRejectionReason());
        response.setAdminMessage(kyc.getAdminMessage());
        response.setSubmissionCount(kyc.getSubmissionCount());
        response.setUpdatedAt(kyc.getUpdatedAt());
        return response;
    }

    @Override
    public List<BusinessDocumentResponse> getDocuments(Long businessId) {
        ensureCurrentUserOwnsBusiness(businessId);

        log.debug("Returning documents for businessId={}", businessId);

        return documentRepository.findByBusinessId(businessId)
                .stream()
                .map(businessDocumentMapper::toResponse)
                .toList();
    }

    private void ensureCurrentUserOwnsBusiness(Long businessId) {
        BusinessMetaData metaData = businessQueryApi.getBusinessDetailsById(businessId);
        Long currentUserId = SecurityUtil.getCurrentUserId();

        if (metaData.getOwnerUserId() == null || !metaData.getOwnerUserId().equals(currentUserId)) {
            throw new InvalidBusinessStateException("Unauthorized: you do not own this business");
        }
    }
}
