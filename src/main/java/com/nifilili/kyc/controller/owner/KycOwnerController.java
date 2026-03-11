package com.nifilili.kyc.controller.owner;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.kyc.dto.response.BusinessDocumentResponse;
import com.nifilili.kyc.dto.response.KycStatusResponse;
import com.nifilili.kyc.service.OwnerKycQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('USER')")
@Tag(name = SwaggerConstants.KYC_2)
public class KycOwnerController {

    private final OwnerKycQueryService ownerKycQueryService;

    @Operation(summary = "Get KYC status for a business",
            description = "Returns the current KYC status including rejection reason (correction banner) for the authenticated owner.")
    @ApiResponse(responseCode = "200", description = "KYC status retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Unauthorized — caller does not own this business")
    @ApiResponse(responseCode = "401", description = "Unauthorized — not authenticated")
    @ApiResponse(responseCode = "404", description = "KYC record not found")
    @GetMapping("/{businessId}/status")
    public ResponseEntity<KycStatusResponse> getStatus(@PathVariable Long businessId) {
        log.info("Owner requesting KYC status for businessId={}", businessId);
        KycStatusResponse status = ownerKycQueryService.getStatus(businessId);
        log.info("Returning KYC status={} for businessId={}", status.getKycStatus(), businessId);
        return ResponseEntity.ok(status);
    }

    @Operation(summary = "Get uploaded KYC documents",
            description = "Returns the list of documents uploaded by the owner, with per-document review status and rejection reasons.")
    @ApiResponse(responseCode = "200", description = "Documents retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Unauthorized — caller does not own this business")
    @ApiResponse(responseCode = "401", description = "Unauthorized — not authenticated")
    @ApiResponse(responseCode = "404", description = "Business not found")
    @GetMapping("/{businessId}/documents")
    public ResponseEntity<List<BusinessDocumentResponse>> getDocuments(@PathVariable Long businessId) {
        log.info("Owner requesting documents for businessId={}", businessId);
        List<BusinessDocumentResponse> documents = ownerKycQueryService.getDocuments(businessId);
        log.info("Returning {} documents for businessId={}", documents.size(), businessId);
        return ResponseEntity.ok(documents);
    }
}
