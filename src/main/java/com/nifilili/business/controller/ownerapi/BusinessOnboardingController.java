package com.nifilili.business.controller.ownerapi;

import com.nifilili.business.dto.request.*;
import com.nifilili.business.dto.response.CreateBusinessResponse;
import com.nifilili.business.dto.response.SubmitBusinessResponse;
import com.nifilili.business.service.BusinessOnboardingService;
import com.nifilili.common.enums.BusinessStatus;
import com.nifilili.common.enums.KycStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/business")
@RequiredArgsConstructor
public class BusinessOnboardingController {

    private final BusinessOnboardingService businessOnboardingService;

    /**
     * STEP 1: Create a new business (DRAFT)
     */
    @PostMapping
    public ResponseEntity<CreateBusinessResponse> createBusiness(
            @Valid @RequestBody CreateBusinessRequest request
    ) {
        Long businessId = businessOnboardingService.createBusiness(request);

        CreateBusinessResponse response = new CreateBusinessResponse(
                businessId,
                BusinessStatus.DRAFT,
                "Business created successfully. Please complete the profile."
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * STEP 2: Update core business profile
     */
    @PutMapping("/{businessId}/profile")
    public ResponseEntity<Void> updateBusinessProfile(
            @PathVariable Long businessId,
            @Valid @RequestBody UpdateBusinessProfileRequest request
    ) {
        businessOnboardingService.updateProfile(businessId, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * STEP 3: Assign categories to the business
     */
    @PutMapping("/{businessId}/categories")
    public ResponseEntity<Void> updateBusinessCategories(
            @PathVariable Long businessId,
            @Valid @RequestBody UpdateBusinessCategoriesRequest request
    ) {
        businessOnboardingService.updateCategories(
                businessId,
                request.getCategoryIds()
        );
        return ResponseEntity.noContent().build();
    }

    /**
     * STEP 4: Save dynamic section data
     */
    @PostMapping("/{businessId}/sections/{sectionId}")
    public ResponseEntity<Void> saveSectionData(
            @PathVariable Long businessId,
            @PathVariable Long sectionId,
            @Valid @RequestBody SaveSectionDataRequest request
    ) {
        businessOnboardingService.saveSectionData(
                businessId,
                sectionId,
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * STEP 5: Save dynamic attributes data
     */
    @PostMapping("{businessId}/attributes")
    public ResponseEntity<Void> saveAttributeData(
            @PathVariable Long businessId,
            @RequestBody SaveBusinessAttributeRequest request
    ) {
        businessOnboardingService.saveAttributeData(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * STEP 6: Submit business for KYC verification
     */
    @PostMapping("/{businessId}/submit")
    public ResponseEntity<SubmitBusinessResponse> submitBusinessForKyc(
            @PathVariable Long businessId
    ) {
        businessOnboardingService.submitForKyc(businessId);

        SubmitBusinessResponse response = new SubmitBusinessResponse(
                businessId,
                BusinessStatus.PENDING,
                KycStatus.PENDING,
                "Business submitted for verification. Our team will review it shortly."
        );

        return ResponseEntity.ok(response);
    }
}
