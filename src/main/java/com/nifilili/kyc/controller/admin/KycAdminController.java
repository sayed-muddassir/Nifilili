package com.nifilili.kyc.controller.admin;

import com.nifilili.common.enums.KycStatus;
import com.nifilili.kyc.dto.request.ReviewKycRequest;
import com.nifilili.kyc.dto.response.AdminKycDetailResponse;
import com.nifilili.kyc.dto.response.AdminKycListItemResponse;
import com.nifilili.kyc.service.AdminKycQueryService;
import com.nifilili.kyc.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/kyc")
@RequiredArgsConstructor
public class KycAdminController {

    private final KycService service;
    private final AdminKycQueryService kycQueryService;

    /**
     * Pending / Approved / Rejected lists
     */
    @Operation(summary = "Get KYC application List by Status",
            description = "Retrieves a paginated list of KYC applications filtered by their status (Pending, Approved, Rejected).",
            tags = {"KYC Management [Admin]"}
    )
    @GetMapping
    public Page<AdminKycListItemResponse> getByStatus(
            @RequestParam KycStatus status,
            Pageable pageable
    ) {
        return kycQueryService.getByStatus(status, pageable);
    }

    /**
     * Full KYC detail view
     */
    @Operation(summary = "Get KYC Application Detail",
            description = "Retrieves detailed information about a specific KYC application by business ID.",
            tags = {"KYC Management [Admin]"}
    )
    @GetMapping("/{businessId}")
    public AdminKycDetailResponse getDetail(
            @PathVariable Long businessId
    ) {
        return kycQueryService.getDetail(businessId);
    }

    /**
     * Review KYC - Approve / Reject
     */
    @Operation(summary = "Review KYC Application",
            description = "Allows admin to review a KYC application and either approve or reject it.",
            tags = {"KYC Management [Admin]"}
    )
    @PostMapping("/{businessId}/review")
    public void review(
            @PathVariable Long businessId,
            @RequestBody ReviewKycRequest request
    ) {
        service.review(businessId, request);
    }
}
