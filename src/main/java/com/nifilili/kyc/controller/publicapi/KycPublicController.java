package com.nifilili.kyc.controller.publicapi;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.core.enums.kyc.KycStatus;
import com.nifilili.kyc.dto.response.VerificationBadgeResponse;
import com.nifilili.kyc.repository.BusinessKycRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/kyc")
@RequiredArgsConstructor
@Slf4j
@Tag(name = SwaggerConstants.BUSINESS_6,
        description = "Public endpoint for checking business verification badge status.")
public class KycPublicController {

    private final BusinessKycRepository kycRepository;

    @Operation(summary = "Check business verification badge",
            description = "Returns whether a business has the 'Nifilili Verified' badge (KYC approved).")
    @ApiResponse(responseCode = "200", description = "Verification status returned")
    @GetMapping("/{businessId}/verified")
    public ResponseEntity<VerificationBadgeResponse> isVerified(@PathVariable Long businessId) {
        log.debug("Checking verification badge for businessId={}", businessId);

        boolean verified = kycRepository.findByBusinessId(businessId)
                .map(kyc -> kyc.getKycStatus() == KycStatus.APPROVED)
                .orElse(false);

        return ResponseEntity.ok(VerificationBadgeResponse.builder()
                .businessId(businessId)
                .verified(verified)
                .build());
    }
}
