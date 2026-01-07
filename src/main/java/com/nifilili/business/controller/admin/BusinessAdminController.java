package com.nifilili.business.controller.admin;

import com.nifilili.business.dto.request.*;
import com.nifilili.business.dto.response.*;
import com.nifilili.business.service.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/config")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ADMIN')")
public class BusinessAdminController {

    private final VerticalDefinitionService verticalDefinitionService;
    private final CategoryDefinitionService categoryDefinitionService;
    private final SectionDefinitionService sectionDefinitionService;
    private final AttributeDefinitionService attributeDefinitionService;
    private final DocumentDefinitionService documentDefinitionService;

    @Operation(summary = "Create VerticalDefinition",
            description = "Creates a new vertical in the system.",
            tags = {"Business Configuration [Admin]"}
    )
    @PostMapping("/verticals")
    public VerticalResponse create(@RequestBody CreateVerticalRequest request) {
        return verticalDefinitionService.create(request);
    }

    @Operation(summary = "Create CategoryDefinition",
            description = "Creates a new category for a specific vertical.",
            tags = {"Business Configuration [Admin]"}
    )
    @PostMapping("/categories")
    public CategoryResponse create(@RequestBody CreateCategoryRequest request) {
        return categoryDefinitionService.create(request);
    }

    @Operation(summary = "Create SectionDefinition",
            description = "Creates a new section for a specific vertical.",
            tags = {"Business Configuration [Admin]"}
    )
    @PostMapping("/sections")
    public SectionResponse createSection(@RequestBody CreateSectionRequest request) {
        return sectionDefinitionService.createSection(request);
    }

    @Operation(summary = "Add Field to SectionDefinition",
            description = "Adds a new field to an existing section.",
            tags = {"Business Configuration [Admin]"}
    )
    @PostMapping("/sections/{sectionId}/fields")
    public SectionFieldResponse addFieldToSection(
            @PathVariable Long sectionId,
            @RequestBody CreateSectionFieldRequest request
    ) {
        return sectionDefinitionService.addField(sectionId, request);
    }

    @Operation(summary = "Create Attribute Definition",
            description = "Creates a new attribute definition for a specific vertical.",
            tags = {"Business Configuration [Admin]"}
    )
    @PostMapping("/attributes")
    public AttributeDefinitionResponse createAttribute(
            @RequestBody CreateAttributeDefinitionRequest request
    ) {
        return attributeDefinitionService.create(request);
    }

    @Operation(summary = "Create Document Definition",
            description = "Creates a new document definition for a specific vertical.",
            tags = {"Business Configuration [Admin]"}
    )
    @PostMapping("/documents")
    public DocumentDefinitionResponse createDocument(
            @RequestBody CreateDocumentDefinitionRequest request
    ) {
        return documentDefinitionService.create(request);
    }
}
