package com.nifilili.offering.controller.admin;

import com.nifilili.offering.domain.OfferingAttributeEntity;
import com.nifilili.offering.dto.request.CreateAttributeRequest;
import com.nifilili.offering.dto.response.AttributeResponse;
import com.nifilili.offering.service.admin.OfferingAttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/offering-attributes")
@RequiredArgsConstructor
public class OfferingAttributeAdminController {

    private final OfferingAttributeService attributeService;

    @PostMapping
    public AttributeResponse create(@RequestBody CreateAttributeRequest request) {
        OfferingAttributeEntity saved =
                attributeService.create(
                        request.offeringCategoryId(),
                        request.name(),
                        request.attributeType(),
                        request.options()
                );

        return new AttributeResponse(
                saved.getId(),
                saved.getCategory().getId(),
                saved.getName(),
                saved.getAttributeType(),
                saved.getOptions()
        );
    }

    @GetMapping("/category/{categoryId}")
    public List<AttributeResponse> byCategory(@PathVariable Long categoryId) {
        return attributeService.getByCategory(categoryId)
                .stream()
                .map(a -> new AttributeResponse(
                        a.getId(),
                        a.getCategory().getId(),
                        a.getName(),
                        a.getAttributeType(),
                        a.getOptions()
                ))
                .toList();
    }
}

