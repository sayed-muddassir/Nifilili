package com.nifilili.config.controller.admin;

import com.nifilili.config.dto.request.CreateAttributeDefinitionRequest;
import com.nifilili.config.dto.response.AttributeDefinitionResponse;
import com.nifilili.config.service.AttributeDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/attributes")
@RequiredArgsConstructor
public class AttributeAdminController {

    private final AttributeDefinitionService service;

    @PostMapping
    public AttributeDefinitionResponse create(
            @RequestBody CreateAttributeDefinitionRequest request
    ) {
        return service.create(request);
    }

    @GetMapping("/vertical/{verticalId}")
    public List<AttributeDefinitionResponse> getByVertical(
            @PathVariable Long verticalId
    ) {
        return service.getByVertical(verticalId);
    }
}
