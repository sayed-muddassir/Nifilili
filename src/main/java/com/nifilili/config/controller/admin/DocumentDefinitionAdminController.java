package com.nifilili.config.controller.admin;

import com.nifilili.config.dto.request.CreateDocumentDefinitionRequest;
import com.nifilili.config.dto.response.DocumentDefinitionResponse;
import com.nifilili.config.service.DocumentDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/config/documents")
@RequiredArgsConstructor
public class DocumentDefinitionAdminController {

    private final DocumentDefinitionService service;

    @PostMapping
    public DocumentDefinitionResponse create(
            @RequestBody CreateDocumentDefinitionRequest request
    ) {
        return service.create(request);
    }

    @GetMapping("/vertical/{verticalId}")
    public List<DocumentDefinitionResponse> getByVertical(
            @PathVariable Long verticalId
    ) {
        return service.getByVertical(verticalId);
    }
}
