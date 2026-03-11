package com.nifilili.kyc.controller.admin;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.core.enums.kyc.KycStatus;
import com.nifilili.kyc.dto.request.ReviewKycRequest;
import com.nifilili.kyc.dto.response.AdminKycDetailResponse;
import com.nifilili.kyc.dto.response.AdminKycListItemResponse;
import com.nifilili.kyc.service.AdminKycQueryService;
import com.nifilili.kyc.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/kyc")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize(value = "hasRole('ADMIN')")
@Tag(name = SwaggerConstants.KYC_1)
public class KycAdminController {

    private final KycService kycService;
    private final AdminKycQueryService kycQueryService;

    @Operation(summary = "Get KYC application List by Status",
            description = "Retrieves a paginated list of KYC applications filtered by their status (Pending, Approved, Rejected).")
    @ApiResponse(responseCode = "200", description = "KYC list retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status parameter")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden — admin role required")
    @GetMapping
    public ResponseEntity<Page<AdminKycListItemResponse>> getByStatus(@RequestParam KycStatus status,
                                                                       Pageable pageable) {
        log.info("Admin requesting KYC list with status={}", status);
        Page<AdminKycListItemResponse> result = kycQueryService.getByStatus(status, pageable);
        log.info("Returning {} KYC records for status={}", result.getTotalElements(), status);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get KYC Application Detail",
            description = "Retrieves detailed information about a specific KYC application by business ID.")
    @ApiResponse(responseCode = "200", description = "KYC detail retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden — admin role required")
    @ApiResponse(responseCode = "404", description = "KYC record not found for the given business ID")
    @GetMapping("/{businessId}")
    public ResponseEntity<AdminKycDetailResponse> getDetail(@PathVariable Long businessId) {
        log.info("Admin requesting KYC detail for businessId={}", businessId);
        AdminKycDetailResponse detail = kycQueryService.getDetail(businessId);
        log.info("Returning KYC detail for businessId={}, status={}", businessId, detail.getKycStatus());
        return ResponseEntity.ok(detail);
    }

    @Operation(summary = "Review KYC Application",
            description = "Allows admin to review a KYC application and either approve or reject it.")
    @ApiResponse(responseCode = "200", description = "KYC review processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or KYC not in reviewable state")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden — admin role required")
    @ApiResponse(responseCode = "404", description = "KYC record not found for the given business ID")
    @PostMapping("/{businessId}/review")
    public ResponseEntity<Void> review(@PathVariable Long businessId,
                                        @Valid @RequestBody ReviewKycRequest request) {
        log.info("Admin reviewing KYC for businessId={}, approve={}", businessId, request.isApprove());
        kycService.review(businessId, request);
        log.info("KYC review completed for businessId={}", businessId);
        return ResponseEntity.ok().build();
    }
}
