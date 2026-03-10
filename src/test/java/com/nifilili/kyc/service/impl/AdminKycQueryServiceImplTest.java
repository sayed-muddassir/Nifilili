package com.nifilili.kyc.service.impl;

import com.nifilili.business.api.BusinessQueryApi;
import com.nifilili.business.api.model.BusinessMetaData;
import com.nifilili.core.enums.kyc.KycStatus;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.kyc.domain.BusinessDocument;
import com.nifilili.kyc.domain.BusinessKyc;
import com.nifilili.kyc.dto.response.AdminKycDetailResponse;
import com.nifilili.kyc.dto.response.AdminKycListItemResponse;
import com.nifilili.kyc.dto.response.BusinessDocumentResponse;
import com.nifilili.kyc.mapper.BusinessDocumentMapper;
import com.nifilili.kyc.repository.BusinessDocumentRepository;
import com.nifilili.kyc.repository.BusinessKycRepository;
import com.nifilili.kyc.repository.projection.AdminKycListProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminKycQueryServiceImplTest {

    @Mock
    private BusinessKycRepository kycRepository;
    @Mock
    private BusinessDocumentRepository documentRepository;
    @Mock
    private BusinessDocumentMapper businessDocumentMapper;
    @Mock
    private BusinessQueryApi businessQueryApi;

    @InjectMocks
    private AdminKycQueryServiceImpl adminKycQueryService;

    @Test
    void getByStatus_WhenPending_ShouldReturnPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Instant now = Instant.now();

        AdminKycListProjection projection = mock(AdminKycListProjection.class);
        when(projection.getBusinessId()).thenReturn(10L);
        when(projection.getBusinessName()).thenReturn("Test Biz");
        when(projection.getVerticalId()).thenReturn(1L);
        when(projection.getKycStatus()).thenReturn("PENDING");
        when(projection.getSubmissionCount()).thenReturn(1);
        when(projection.getUpdatedAt()).thenReturn(now);

        Page<AdminKycListProjection> projectionPage =
                new PageImpl<>(List.of(projection), pageable, 1);

        when(kycRepository.findByStatus("PENDING", pageable)).thenReturn(projectionPage);

        Page<AdminKycListItemResponse> result =
                adminKycQueryService.getByStatus(KycStatus.PENDING, pageable);

        assertEquals(1, result.getTotalElements());
        AdminKycListItemResponse item = result.getContent().get(0);
        assertEquals(10L, item.getBusinessId());
        assertEquals("Test Biz", item.getBusinessName());
        assertEquals(1L, item.getVerticalId());
        assertEquals(KycStatus.PENDING, item.getKycStatus());
        assertEquals(1, item.getSubmissionCount());
        assertEquals(now, item.getLastUpdatedAt());

        verify(kycRepository).findByStatus("PENDING", pageable);
    }

    @Test
    void getDetail_WhenKycExists_ShouldReturnDetailWithDocuments() {
        Long businessId = 10L;

        BusinessKyc kyc = new BusinessKyc();
        kyc.setBusinessId(businessId);
        kyc.setKycStatus(KycStatus.PENDING);
        kyc.setAdminMessage("Reviewing documents");
        kyc.setRejectionReason(null);
        kyc.setSubmissionCount(2);

        BusinessDocument document = BusinessDocument.builder()
                .businessId(businessId)
                .documentDefinitionId(5L)
                .fileName("license.pdf")
                .fileUrl("https://files.example.com/license.pdf")
                .build();

        BusinessDocumentResponse documentResponse = new BusinessDocumentResponse();
        documentResponse.setId(100L);
        documentResponse.setDocumentDefinitionId(5L);
        documentResponse.setFileName("license.pdf");
        documentResponse.setFileUrl("https://files.example.com/license.pdf");

        BusinessMetaData metaData = new BusinessMetaData();
        metaData.setBusinessId(businessId);
        metaData.setBusinessName("Test Biz");
        metaData.setVerticalId(1L);
        metaData.setOwnerUserId(99L);

        when(kycRepository.findByBusinessId(businessId)).thenReturn(Optional.of(kyc));
        when(documentRepository.findByBusinessId(businessId)).thenReturn(List.of(document));
        when(businessDocumentMapper.toResponse(document)).thenReturn(documentResponse);
        when(businessQueryApi.getBusinessDetailsById(businessId)).thenReturn(metaData);

        AdminKycDetailResponse result = adminKycQueryService.getDetail(businessId);

        assertEquals(businessId, result.getBusinessId());
        assertEquals("Test Biz", result.getBusinessName());
        assertEquals(1L, result.getVerticalId());
        assertEquals(KycStatus.PENDING, result.getKycStatus());
        assertEquals("Reviewing documents", result.getAdminMessage());
        assertNull(result.getRejectionReason());
        assertEquals(2, result.getSubmissionCount());
        assertEquals(1, result.getDocuments().size());
        assertEquals("license.pdf", result.getDocuments().get(0).getFileName());

        verify(kycRepository).findByBusinessId(businessId);
        verify(documentRepository).findByBusinessId(businessId);
        verify(businessDocumentMapper).toResponse(document);
        verify(businessQueryApi).getBusinessDetailsById(businessId);
    }

    @Test
    void getDetail_WhenKycNotFound_ShouldThrowResourceNotFound() {
        Long businessId = 999L;

        when(kycRepository.findByBusinessId(businessId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminKycQueryService.getDetail(businessId));

        verify(kycRepository).findByBusinessId(businessId);
        verifyNoInteractions(documentRepository);
        verifyNoInteractions(businessDocumentMapper);
        verifyNoInteractions(businessQueryApi);
    }
}
