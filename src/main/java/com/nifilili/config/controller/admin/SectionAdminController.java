package com.nifilili.config.controller.admin;

import com.nifilili.config.dto.request.CreateSectionFieldRequest;
import com.nifilili.config.dto.request.CreateSectionRequest;
import com.nifilili.config.dto.response.SectionFieldResponse;
import com.nifilili.config.dto.response.SectionResponse;
import com.nifilili.config.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/config/sections")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ADMIN')")
public class SectionAdminController {

    private final SectionService service;

    @Operation(summary = "Create Section",
            description = "Creates a new section for a specific vertical.",
            tags = {"Section Management [Admin]"}
    )
    @PostMapping
    public SectionResponse createSection(@RequestBody CreateSectionRequest request) {
        return service.createSection(request);
    }

    @Operation(summary = "Add Field to Section",
            description = "Adds a new field to an existing section.",
            tags = {"Section Management [Admin]"}
    )
    @PostMapping("/{sectionId}/fields")
    public SectionFieldResponse addField(
            @PathVariable Long sectionId,
            @RequestBody CreateSectionFieldRequest request
    ) {
        return service.addField(sectionId, request);
    }

    @Operation(summary = "Get Sections by Vertical",
            description = "Retrieves all sections associated with a specific vertical.",
            tags = {"Section Management [Admin]"}
    )
    @GetMapping("/vertical/{verticalId}")
    public List<SectionResponse> getByVertical(@PathVariable Long verticalId) {
        return service.getByVertical(verticalId);
    }

    @Operation(summary = "Get Fields by Section",
            description = "Retrieves all fields associated with a specific section.",
            tags = {"Section Management [Admin]"}
    )
    @GetMapping("/{sectionId}/fields")
    public List<SectionFieldResponse> getFields(@PathVariable Long sectionId) {
        return service.getFields(sectionId);
    }
}
