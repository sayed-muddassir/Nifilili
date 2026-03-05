package com.nifilili.business.controller.owner;

import com.nifilili.business.dto.request.*;
import com.nifilili.business.dto.response.*;
import com.nifilili.business.service.*;
import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.core.enums.business.BusinessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/business")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('USER')")
@Tag(
        name = SwaggerConstants.BUSINESS_2,
        description = "Chronological onboarding APIs: fetch config, create business, complete profile, upload docs, submit for review."
)
public class BusinessOnboardingController {

    private final BusinessOnboardingService businessOnboardingService;
    private final BusinessProfileService businessProfileService;
    private final BusinessCategoryService businessCategoryService;
    private final BusinessSectionService businessSectionService;
    private final BusinessAttributeService businessAttributeService;
    private final VerticalDefinitionService verticalDefinitionService;
    private final CategoryDefinitionService categoryDefinitionService;
    private final SectionDefinitionService sectionDefinitionService;
    private final AttributeDefinitionService attributeDefinitionService;
    private final DocumentDefinitionService documentDefinitionService;
    private final BusinessPublishService businessPublishService;

    @Operation(summary = "Step 0.1: Get Active Verticals", description = "Loads active verticals to start onboarding.")
    @GetMapping("/verticals/active")
    public List<VerticalResponse> getAllActiveVerticals() {
        return verticalDefinitionService.getAllActive();
    }

    @Operation(summary = "Step 0.2: Get Categories by Vertical", description = "Loads categories under selected vertical.")
    @GetMapping("/vertical/{verticalId}/categories")
    public List<CategoryResponse> getCategoriesByVertical(@PathVariable Long verticalId) {
        return categoryDefinitionService.getByVertical(verticalId);
    }

    @Operation(summary = "Step 0.3: Get Sections by Vertical", description = "Loads sections for selected vertical.")
    @GetMapping("/vertical/{verticalId}/sections")
    public List<SectionResponse> getSectionsByVertical(@PathVariable Long verticalId) {
        return sectionDefinitionService.getByVertical(verticalId);
    }

    @Operation(summary = "Step 0.4: Get Section Fields", description = "Loads field definitions for a selected section.")
    @GetMapping("/sections/{sectionId}/fields")
    public List<SectionFieldResponse> getSectionFields(@PathVariable Long sectionId) {
        return sectionDefinitionService.getFields(sectionId);
    }

    @Operation(summary = "Step 0.5: Get Attributes by Vertical", description = "Loads attribute definitions for selected vertical.")
    @GetMapping("/vertical/{verticalId}/attributes")
    public List<AttributeDefinitionResponse> getAttributesByVertical(@PathVariable Long verticalId) {
        return attributeDefinitionService.getByVertical(verticalId);
    }

    @Operation(summary = "Step 0.6: Get Documents by Vertical", description = "Loads required KYC documents for selected vertical.")
    @GetMapping("/vertical/{verticalId}/documents")
    public List<DocumentDefinitionResponse> getDocumentByVertical(@PathVariable Long verticalId) {
        return documentDefinitionService.getByVertical(verticalId);
    }

    @Operation(summary = "Step 1: Create Business", description = "Creates a draft business record and returns onboarding id.")
    @PostMapping
    public ResponseEntity<CreateBusinessResponse> createBusiness(@Valid @RequestBody CreateBusinessRequest request) {
        Long businessId = businessOnboardingService.createBusiness(request);

        CreateBusinessResponse response = new CreateBusinessResponse(
                businessId,
                BusinessStatus.DRAFT,
                "Business created successfully. Continue with profile completion."
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Step 2: Update Business Profile", description = "Updates core profile information.")
    @PutMapping("/{businessId}/profile")
    public ResponseEntity<Void> updateBusinessProfile(
            @PathVariable Long businessId,
            @Valid @RequestBody UpdateBusinessProfileRequest request
    ) {
        businessProfileService.updateProfile(businessId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Step 3: Update Business Categories", description = "Assigns categories for the business.")
    @PutMapping("/{businessId}/categories")
    public ResponseEntity<Void> updateBusinessCategories(
            @PathVariable Long businessId,
            @Valid @RequestBody UpdateBusinessCategoriesRequest request
    ) {
        businessCategoryService.updateCategories(businessId, request.getCategoryIds());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Step 4: Save Section Data", description = "Saves values for one section block.")
    @PostMapping("/{businessId}/sections/{sectionId}")
    public ResponseEntity<Void> saveSectionData(
            @PathVariable Long businessId,
            @PathVariable Long sectionId,
            @Valid @RequestBody SaveSectionDataRequest request
    ) {
        businessSectionService.saveSectionData(businessId, sectionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Step 5: Save Attribute Data", description = "Saves one attribute value for the business.")
    @PutMapping("/{businessId}/attributes")
    public ResponseEntity<Void> saveAttributeData(
            @PathVariable Long businessId,
            @Valid @RequestBody SaveBusinessAttributeRequest request
    ) {
        businessAttributeService.saveAttributeData(businessId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Step 6: Upload KYC Document", description = "Uploads one KYC document for review.")
    @PostMapping("/{businessId}/documents")
    public ResponseEntity<Void> uploadDocument(
            @PathVariable Long businessId,
            @Valid @RequestBody UploadBusinessDocumentRequest request
    ) {
        businessPublishService.uploadVerificationDocuments(businessId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Operation(summary = "Step 7: Submit for Review", description = "Submits onboarding + KYC for admin review.")
    @PostMapping("/{businessId}/submit")
    public ResponseEntity<Void> submitForReview(
            @PathVariable Long businessId,
            @Valid @RequestBody SubmitBusinessForReviewRequest request
    ) {
        businessPublishService.submitForVerification(businessId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Operation(
            summary = "Step 8: Claim Admin-Seeded Business",
            description = "Claims an unclaimed admin-seeded business listing. Sets the authenticated user as the owner and initiates the KYC verification process."
    )
    @ApiResponse(responseCode = "202", description = "Business claimed, KYC process initiated")
    @ApiResponse(responseCode = "400", description = "Business is not claimable (not admin-seeded or already claimed)")
    @ApiResponse(responseCode = "404", description = "Business not found")
    @PostMapping("/{businessId}/claim")
    public ResponseEntity<Void> claimBusiness(@PathVariable Long businessId) {
        businessPublishService.claimBusiness(businessId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
