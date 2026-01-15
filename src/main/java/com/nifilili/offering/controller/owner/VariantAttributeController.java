package com.nifilili.offering.controller.owner;

import com.nifilili.offering.dto.request.AssignVariantAttributesRequest;
import com.nifilili.offering.dto.response.VariantAttributeResponse;
import com.nifilili.offering.service.owner.VariantAttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/variants/{variantId}/attributes")
@RequiredArgsConstructor
public class VariantAttributeController {

    private final VariantAttributeService service;

    @PostMapping
    public VariantAttributeResponse assign(
            @PathVariable Long variantId,
            @RequestBody AssignVariantAttributesRequest request) {

        service.assignAttributes(variantId, request);

        return new VariantAttributeResponse(variantId, true);
    }
}

