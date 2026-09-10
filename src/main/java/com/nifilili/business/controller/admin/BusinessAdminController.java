package com.nifilili.business.controller.admin;

import com.nifilili.business.dto.request.*;
import com.nifilili.business.dto.response.*;
import com.nifilili.business.service.*;
import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.core.enums.business.BusinessSource;
import com.nifilili.core.enums.business.BusinessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/config")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ADMIN')")
@Tag(name = SwaggerConstants.BUSINESS_1)
public class BusinessAdminController {

    private final VerticalDefinitionService verticalDefinitionService;
    private final CategoryDefinitionService categoryDefinitionService;
    private final SectionDefinitionService sectionDefinitionService;
    private final AttributeDefinitionService attributeDefinitionService;
    private final DocumentDefinitionService documentDefinitionService;
    private final BusinessOnboardingService businessOnboardingService;

    @Operation(summary = "Step 1.0: List Verticals", description = "Retrieves all business verticals.")
    @GetMapping("/verticals")
    public List<VerticalResponse> getAllVerticals() {
        return verticalDefinitionService.getAll();
    }

    @Operation(summary = "Step 1.1: Create Vertical", description = "Creates a new top-level business vertical.")
    @PostMapping("/verticals")
    public VerticalResponse createVertical(@Valid @RequestBody CreateVerticalRequest request) {
        return verticalDefinitionService.create(request);
    }

    @Operation(summary = "Step 1.2: Update Vertical", description = "Updates an existing vertical.")
    @PutMapping("/verticals/{verticalId}")
    public VerticalResponse updateVertical(@PathVariable Long verticalId, @Valid @RequestBody CreateVerticalRequest request) {
        return verticalDefinitionService.update(verticalId, request);
    }

    @Operation(summary = "Step 1.3: Delete Vertical", description = "Deletes an existing vertical.")
    @DeleteMapping("/verticals/{verticalId}")
    public void deleteVertical(@PathVariable Long verticalId) {
        verticalDefinitionService.delete(verticalId);
    }

    @Operation(summary = "Step 2.0: List Categories by Vertical", description = "Loads categories under selected vertical.")
    @GetMapping("/vertical/{verticalId}/categories")
    public List<CategoryResponse> getCategoriesByVertical(@PathVariable Long verticalId) {
        return categoryDefinitionService.getByVertical(verticalId);
    }

    @Operation(summary = "Step 2.1: Create Category", description = "Creates a category under a vertical.")
    @PostMapping("/categories")
    public CategoryResponse createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryDefinitionService.create(request);
    }

    @Operation(summary = "Step 2.2: Update Category", description = "Updates an existing category.")
    @PutMapping("/categories/{categoryId}")
    public CategoryResponse updateCategory(@PathVariable Long categoryId, @Valid @RequestBody CreateCategoryRequest request) {
        return categoryDefinitionService.update(categoryId, request);
    }

    @Operation(summary = "Step 2.3: Delete Category", description = "Deletes an existing category.")
    @DeleteMapping("/categories/{categoryId}")
    public void deleteCategory(@PathVariable Long categoryId) {
        categoryDefinitionService.delete(categoryId);
    }

    @Operation(summary = "Step 3.0: List Sections by Vertical", description = "Loads sections for selected vertical.")
    @GetMapping("/vertical/{verticalId}/sections")
    public List<SectionResponse> getSectionsByVertical(@PathVariable Long verticalId) {
        return sectionDefinitionService.getByVertical(verticalId);
    }

    @Operation(summary = "Step 3.1: Create Section", description = "Creates a section definition for a vertical and optional category.")
    @PostMapping("/sections")
    public SectionResponse createSection(@Valid @RequestBody CreateSectionRequest request) {
        return sectionDefinitionService.createSection(request);
    }

    @Operation(summary = "Step 3.2: Update Section", description = "Updates a section definition.")
    @PutMapping("/sections/{sectionId}")
    public SectionResponse updateSection(@PathVariable Long sectionId, @Valid @RequestBody CreateSectionRequest request) {
        return sectionDefinitionService.updateSection(sectionId, request);
    }

    @Operation(summary = "Step 3.3: Delete Section", description = "Deletes an existing section.")
    @DeleteMapping("/sections/{sectionId}")
    public void deleteSection(@PathVariable Long sectionId) {
        sectionDefinitionService.delete(sectionId);
    }

    @Operation(summary = "Step 4.0: List Section Fields", description = "Loads field definitions for a selected section.")
    @GetMapping("/sections/{sectionId}/fields")
    public List<SectionFieldResponse> getSectionFields(@PathVariable Long sectionId) {
        return sectionDefinitionService.getFields(sectionId);
    }

    @Operation(summary = "Step 4.1: Create Section Field", description = "Adds a field to an existing section.")
    @PostMapping("/sections/{sectionId}/fields")
    public SectionFieldResponse createSectionField(@PathVariable Long sectionId, @Valid @RequestBody CreateSectionFieldRequest request) {
        return sectionDefinitionService.addField(sectionId, request);
    }

    @Operation(summary = "Step 4.2: Update Section Field", description = "Updates an existing section field.")
    @PutMapping("/sections/fields/{fieldId}")
    public SectionFieldResponse updateSectionField(@PathVariable Long fieldId, @Valid @RequestBody CreateSectionFieldRequest request) {
        return sectionDefinitionService.updateField(fieldId, request);
    }

    @Operation(summary = "Step 4.3: Delete Section Field", description = "Delete an existing section field.")
    @DeleteMapping("/sections/fields/{fieldId}")
    public void deleteSectionField(@PathVariable Long fieldId) {
        sectionDefinitionService.deleteField(fieldId);
    }

    @Operation(summary = "Step 5.0: List Attributes by Vertical", description = "Loads attribute definitions for selected vertical.")
    @GetMapping("/vertical/{verticalId}/attributes")
    public List<AttributeDefinitionResponse> getAttributesByVertical(@PathVariable Long verticalId) {
        return attributeDefinitionService.getByVertical(verticalId);
    }

    @Operation(summary = "Step 5.1: Create Attribute", description = "Creates a reusable attribute definition.")
    @PostMapping("/attributes")
    public AttributeDefinitionResponse createAttribute(@Valid @RequestBody CreateAttributeDefinitionRequest request) {
        return attributeDefinitionService.create(request);
    }

    @Operation(summary = "Step 5.2: Update Attribute", description = "Updates an existing attribute definition.")
    @PutMapping("/attributes/{attributeId}")
    public AttributeDefinitionResponse updateAttribute(@PathVariable Long attributeId, @Valid @RequestBody CreateAttributeDefinitionRequest request) {
        return attributeDefinitionService.update(attributeId, request);
    }

    @Operation(summary = "Step 5.3: Delete Attribute", description = "Deletes an existing attribute definition.")
    @DeleteMapping("/attributes/{attributeId}")
    public void deleteAttribute(@PathVariable Long attributeId) {
        attributeDefinitionService.delete(attributeId);
    }

    @Operation(summary = "Step 6.0: List Documents by Vertical", description = "Loads required KYC documents for selected vertical.")
    @GetMapping("/vertical/{verticalId}/documents")
    public List<DocumentDefinitionResponse> getDocumentByVertical(@PathVariable Long verticalId) {
        return documentDefinitionService.getByVertical(verticalId);
    }

    @Operation(summary = "Step 6.1: Create Document Definition", description = "Creates a KYC document requirement definition.")
    @PostMapping("/documents")
    public DocumentDefinitionResponse createDocument(@Valid @RequestBody CreateDocumentDefinitionRequest request) {
        return documentDefinitionService.create(request);
    }

    @Operation(summary = "Step 6.2: Update Document Definition", description = "Updates an existing KYC document requirement definition.")
    @PutMapping("/documents/{documentDefinitionId}")
    public DocumentDefinitionResponse updateDocument(@PathVariable Long documentDefinitionId, @Valid @RequestBody CreateDocumentDefinitionRequest request) {
        return documentDefinitionService.update(documentDefinitionId, request);
    }

    @Operation(summary = "Step 6.3: Delete Document Definition", description = "Deletes an existing KYC document requirement definition.")
    @DeleteMapping("/documents/{documentDefinitionId}")
    public void deleteDocument(@PathVariable Long documentDefinitionId) {
        documentDefinitionService.delete(documentDefinitionId);
    }

    @Operation(summary = "Step 7.0: Create Business (Admin Seed)", description = "Creates a admin seeded business record and returns onboarding id.")
    @PostMapping("/businesses")
    public ResponseEntity<CreateBusinessResponse> createBusiness(@Valid @RequestBody CreateBusinessRequest request) {
        Long businessId = businessOnboardingService.createBusiness(request, BusinessSource.ADMIN_SEEDED);

        CreateBusinessResponse response = new CreateBusinessResponse(businessId, BusinessStatus.PUBLISHED, "Admin Seeded business created successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Step 8.0: Add Businesses in Bulk (Admin Seed)", description = "Creates a admin seeded bulk business records and returns count of success and error count")
    @PostMapping("/businesses/bulk")
    public ResponseEntity<BulkCreateBusinessResponse> createBusinesses(@Valid @RequestBody List<CreateBusinessRequest> requests) {
        BulkCreateBusinessResponse bulkCreateBusinessResponse = businessOnboardingService.bulkCreateBusinesses(requests, BusinessSource.ADMIN_SEEDED);
        return ResponseEntity.status(HttpStatus.CREATED).body(bulkCreateBusinessResponse);
    }

}
