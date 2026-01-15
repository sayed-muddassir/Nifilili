package com.nifilili.business.controller.owner;

import com.nifilili.business.dto.request.*;
import com.nifilili.business.dto.response.*;
import com.nifilili.business.service.*;
import com.nifilili.core.enums.business.BusinessStatus;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/business")
@RequiredArgsConstructor
//@PreAuthorize(value = "hasRole('USER')")
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
        businessProfileService.updateProfile(businessId, request);
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
        businessCategoryService.updateCategories(
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
        businessSectionService.saveSectionData(
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
        businessAttributeService.saveAttributeData(businessId, request);
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
        businessPublishService.uploadVerificationDocuments(businessId, request);
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
            @RequestBody SubmitBusinessForReviewRequest request
    ) {
        businessPublishService.submitForVerification(businessId, request);
    }

    // APIs Endpoints for retrieving configuration data (verticals, categories, sections, attributes, document definitions)
    @Operation(summary = "Get All Active Verticals",
            description = "Retrieves all active verticals in the system.",
            tags = {"Business Onboarding [User]"}
    )
    @GetMapping("/verticals/active")
    public List<VerticalResponse> getAllActiveVerticals() {
        return verticalDefinitionService.getAllActive();
    }

    @Operation(summary = "Get Categories by VerticalDefinition",
            description = "Retrieves all categories associated with a specific vertical.",
            tags = {"Business Onboarding [User]"}
    )
    @GetMapping("/vertical/{verticalId}/categories")
    public List<CategoryResponse> getCategoriesByVertical(@PathVariable Long verticalId) {
        return categoryDefinitionService.getByVertical(verticalId);
    }

    @Operation(summary = "Get Sections by VerticalDefinition",
            description = "Retrieves all sections associated with a specific vertical.",
            tags = {"Business Onboarding [User]"}
    )
    @GetMapping("/vertical/{verticalId}/sections")
    public List<SectionResponse> getSectionsByVertical(@PathVariable Long verticalId) {
        return sectionDefinitionService.getByVertical(verticalId);
    }

    @Operation(summary = "Get Fields by SectionDefinition",
            description = "Retrieves all fields associated with a specific section.",
            tags = {"Business Onboarding [User]"}
    )
    @GetMapping("/{sectionId}/fields")
    public List<SectionFieldResponse> getSectionFields(@PathVariable Long sectionId) {
        return sectionDefinitionService.getFields(sectionId);
    }

    @Operation(summary = "Get Attributes by VerticalDefinition",
            description = "Retrieves all attribute definitions associated with a specific vertical.",
            tags = {"Business Onboarding [User]"}
    )
    @GetMapping("/vertical/{verticalId}/attributes")
    public List<AttributeDefinitionResponse> getAttributesByVertical(
            @PathVariable Long verticalId
    ) {
        return attributeDefinitionService.getByVertical(verticalId);
    }


    @Operation(summary = "Get Documents by VerticalDefinition",
            description = "Retrieves all document definitions associated with a specific vertical.",
            tags = {"Business Onboarding [User]"}
    )
    @GetMapping("/vertical/{verticalId}/documents")
    public List<DocumentDefinitionResponse> getDocumentByVertical(
            @PathVariable Long verticalId
    ) {
        return documentDefinitionService.getByVertical(verticalId);
    }
}
