package com.nifilili.business.controller.admin;

import com.nifilili.business.dto.request.*;
import com.nifilili.business.dto.response.*;
import com.nifilili.business.service.*;
import com.nifilili.core.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/config")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ADMIN')")
@Tag(
        name = SwaggerConstants.BUSINESS_1,
        description = "Admin APIs to manage business onboarding master definitions in a predictable sequence."
)
public class BusinessAdminController {

    private final VerticalDefinitionService verticalDefinitionService;
    private final CategoryDefinitionService categoryDefinitionService;
    private final SectionDefinitionService sectionDefinitionService;
    private final AttributeDefinitionService attributeDefinitionService;
    private final DocumentDefinitionService documentDefinitionService;

    @Operation(summary = "1.1 Verticals: Create", description = "Creates a new top-level business vertical.")
    @PostMapping("/verticals")
    public VerticalResponse createVertical(@RequestBody CreateVerticalRequest request) {
        return verticalDefinitionService.create(request);
    }

    @Operation(summary = "1.2 Verticals: Update", description = "Updates an existing vertical.")
    @PutMapping("/verticals/{verticalId}")
    public VerticalResponse updateVertical(
            @PathVariable Long verticalId,
            @RequestBody CreateVerticalRequest request
    ) {
        return verticalDefinitionService.update(verticalId, request);
    }

    @Operation(summary = "2.1 Categories: Create", description = "Creates a category under a vertical.")
    @PostMapping("/categories")
    public CategoryResponse createCategory(@RequestBody CreateCategoryRequest request) {
        return categoryDefinitionService.create(request);
    }

    @Operation(summary = "2.2 Categories: Update", description = "Updates an existing category.")
    @PutMapping("/categories/{categoryId}")
    public CategoryResponse updateCategory(
            @PathVariable Long categoryId,
            @RequestBody CreateCategoryRequest request
    ) {
        return categoryDefinitionService.update(categoryId, request);
    }

    @Operation(summary = "3.1 Sections: Create", description = "Creates a section definition for a vertical and optional category.")
    @PostMapping("/sections")
    public SectionResponse createSection(@RequestBody CreateSectionRequest request) {
        return sectionDefinitionService.createSection(request);
    }

    @Operation(summary = "3.2 Sections: Update", description = "Updates a section definition.")
    @PutMapping("/sections/{sectionId}")
    public SectionResponse updateSection(
            @PathVariable Long sectionId,
            @RequestBody CreateSectionRequest request
    ) {
        return sectionDefinitionService.updateSection(sectionId, request);
    }

    @Operation(summary = "4.1 Section Fields: Create", description = "Adds a field to an existing section.")
    @PostMapping("/sections/{sectionId}/fields")
    public SectionFieldResponse createSectionField(
            @PathVariable Long sectionId,
            @RequestBody CreateSectionFieldRequest request
    ) {
        return sectionDefinitionService.addField(sectionId, request);
    }

    @Operation(summary = "4.2 Section Fields: Update", description = "Updates an existing section field.")
    @PutMapping("/sections/fields/{fieldId}")
    public SectionFieldResponse updateSectionField(
            @PathVariable Long fieldId,
            @RequestBody CreateSectionFieldRequest request
    ) {
        return sectionDefinitionService.updateField(fieldId, request);
    }

    @Operation(summary = "5.1 Attributes: Create", description = "Creates a reusable attribute definition.")
    @PostMapping("/attributes")
    public AttributeDefinitionResponse createAttribute(@RequestBody CreateAttributeDefinitionRequest request) {
        return attributeDefinitionService.create(request);
    }

    @Operation(summary = "5.2 Attributes: Update", description = "Updates an existing attribute definition.")
    @PutMapping("/attributes/{attributeId}")
    public AttributeDefinitionResponse updateAttribute(
            @PathVariable Long attributeId,
            @RequestBody CreateAttributeDefinitionRequest request
    ) {
        return attributeDefinitionService.update(attributeId, request);
    }

    @Operation(summary = "6.1 Document Definitions: Create", description = "Creates a KYC document requirement definition.")
    @PostMapping("/documents")
    public DocumentDefinitionResponse createDocument(@RequestBody CreateDocumentDefinitionRequest request) {
        return documentDefinitionService.create(request);
    }

    @Operation(summary = "6.2 Document Definitions: Update", description = "Updates an existing KYC document requirement definition.")
    @PutMapping("/documents/{documentDefinitionId}")
    public DocumentDefinitionResponse updateDocument(
            @PathVariable Long documentDefinitionId,
            @RequestBody CreateDocumentDefinitionRequest request
    ) {
        return documentDefinitionService.update(documentDefinitionId, request);
    }
}
