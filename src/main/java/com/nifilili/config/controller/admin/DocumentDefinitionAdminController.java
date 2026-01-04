package com.nifilili.config.controller.admin;

import com.nifilili.config.dto.request.CreateDocumentDefinitionRequest;
import com.nifilili.config.dto.response.DocumentDefinitionResponse;
import com.nifilili.config.service.DocumentDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/config/documents")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ADMIN')")
public class DocumentDefinitionAdminController {

    private final DocumentDefinitionService service;

    @Operation(summary = "Create Document Definition",
            description = "Creates a new document definition for a specific vertical.",
            tags = {"Document Management [Admin]"}
    )
    @PostMapping
    public DocumentDefinitionResponse create(
            @RequestBody CreateDocumentDefinitionRequest request
    ) {
        return service.create(request);
    }

    @Operation(summary = "Get Documents by Vertical",
            description = "Retrieves all document definitions associated with a specific vertical.",
            tags = {"Document Management [Admin]"}
    )
    @GetMapping("/vertical/{verticalId}")
    public List<DocumentDefinitionResponse> getByVertical(
            @PathVariable Long verticalId
    ) {
        return service.getByVertical(verticalId);
    }
}
