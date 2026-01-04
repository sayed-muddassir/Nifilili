package com.nifilili.config.controller.admin;

import com.nifilili.config.dto.request.CreateAttributeDefinitionRequest;
import com.nifilili.config.dto.response.AttributeDefinitionResponse;
import com.nifilili.config.service.AttributeDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/attributes")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ADMIN')")
public class AttributeAdminController {

    private final AttributeDefinitionService service;

    @Operation(summary = "Create Attribute Definition",
            description = "Creates a new attribute definition for a specific vertical.",
            tags = {"Attribute Management [Admin]"}
    )
    @PostMapping
    public AttributeDefinitionResponse create(
            @RequestBody CreateAttributeDefinitionRequest request
    ) {
        return service.create(request);
    }

    @Operation(summary = "Get Attributes by Vertical",
            description = "Retrieves all attribute definitions associated with a specific vertical.",
            tags = {"Attribute Management [Admin]"}
    )
    @GetMapping("/vertical/{verticalId}")
    public List<AttributeDefinitionResponse> getByVertical(
            @PathVariable Long verticalId
    ) {
        return service.getByVertical(verticalId);
    }
}
