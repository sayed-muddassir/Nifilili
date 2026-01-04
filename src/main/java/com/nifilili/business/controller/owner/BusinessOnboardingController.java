package com.nifilili.business.controller.owner;

import com.nifilili.business.dto.request.*;
import com.nifilili.business.dto.response.CreateBusinessResponse;
import com.nifilili.business.service.BusinessOnboardingService;
import com.nifilili.common.enums.BusinessStatus;
import com.nifilili.kyc.dto.request.SubmitKycRequest;
import com.nifilili.kyc.dto.request.UploadBusinessDocumentRequest;
import com.nifilili.kyc.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/business")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('USER')")
public class BusinessOnboardingController {

    private final BusinessOnboardingService businessOnboardingService;
    private final KycService kycService;

    /**
     * STEP 1: Create a new business (DRAFT)
     */
    @Operation(summary = "Create a new business",
            description = "Initiates the business onboarding process by creating a new business in DRAFT status.",
            tags = {"Business Onboarding [User]"}
    )
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
    @Operation(summary = "Update business profile",
            description = "Updates the core profile information of the business.",
            tags = {"Business Onboarding [User]"}
    )
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
    @Operation(summary = "Update business categories",
            description = "Updates the categories associated with the business.",
            tags = {"Business Onboarding [User]"}
    )
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
    @Operation(summary = "Save business section data",
            description = "Saves data for a specific dynamic section of the business profile.",
            tags = {"Business Onboarding [User]"}
    )
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
    @Operation(summary = "Save business attribute data",
            description = "Saves dynamic attribute data for the business profile.",
            tags = {"Business Onboarding [User]"}
    )
    @PutMapping("{businessId}/attributes")
    public ResponseEntity<Void> saveAttributeData(
            @PathVariable Long businessId,
            @RequestBody SaveBusinessAttributeRequest request
    ) {
        businessOnboardingService.saveAttributeData(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * STEP 6: Save business documents
     */
    @Operation(summary = "Upload business document",
            description = "Uploads a document required for KYC verification.",
            tags = {"Business Onboarding [User]"})
    @PostMapping("{businessId}/documents")
    public void uploadDocument(
            @PathVariable Long businessId,
            @RequestBody UploadBusinessDocumentRequest request
    ) {
        kycService.uploadDocument(businessId, request);
    }

    /**
     * STEP 7: Submit business for KYC verification
     */
    @Operation(summary = "Submit KYC for review",
            description = "Submits the KYC and information for review by the admin.",
            tags = {"Business Onboarding [User]"}
    )
    @PostMapping("{businessId}/submit")
    public void submit(
            @PathVariable Long businessId,
            @RequestBody SubmitKycRequest request
    ) {
        kycService.submit(businessId, request.getMessage());
    }
}
