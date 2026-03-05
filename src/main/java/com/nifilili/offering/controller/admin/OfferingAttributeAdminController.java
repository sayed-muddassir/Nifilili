package com.nifilili.offering.controller.admin;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.offering.domain.OfferingAttributeEntity;
import com.nifilili.offering.dto.request.CreateAttributeRequest;
import com.nifilili.offering.dto.request.UpdateAttributeRequest;
import com.nifilili.offering.dto.response.AttributeResponse;
import com.nifilili.offering.dto.response.StatusResponse;
import com.nifilili.offering.service.OfferingAttributeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/offering-attributes")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ADMIN')")
@Tag(name = SwaggerConstants.OFFERING_1, description = "Admin setup for offering categories and attributes.")
public class OfferingAttributeAdminController {

    private final OfferingAttributeService attributeService;

    @PostMapping
    @Operation(summary = "Step 1.7: Create Attribute",
            description = "Creates a new attribute definition for a category (e.g., Color, Size)")
    @ApiResponse(responseCode = "200", description = "Attribute created")
    @ApiResponse(responseCode = "400", description = "Category not found")
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
    @Operation(summary = "Step 1.8: Get Attributes by Category",
            description = "Returns all attribute definitions for a given category")
    @ApiResponse(responseCode = "200", description = "Attributes returned")
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

    @PutMapping("/{id}")
    @Operation(summary = "Step 1.9: Update Attribute",
            description = "Updates an attribute definition's name, type, and allowed options")
    @ApiResponse(responseCode = "200", description = "Attribute updated")
    @ApiResponse(responseCode = "400", description = "Attribute not found")
    public AttributeResponse update(@PathVariable Long id, @RequestBody UpdateAttributeRequest request) {
        log.info("PUT /api/v1/admin/offering-attributes/{}", id);
        OfferingAttributeEntity updated = attributeService.update(
                id, request.name(), request.attributeType(), request.options()
        );
        return new AttributeResponse(
                updated.getId(),
                updated.getCategory().getId(),
                updated.getName(),
                updated.getAttributeType(),
                updated.getOptions()
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Step 1.10: Delete Attribute",
            description = "Deletes an attribute definition if it is not in use by any variant attributes")
    @ApiResponse(responseCode = "200", description = "Attribute deleted")
    @ApiResponse(responseCode = "400", description = "Attribute not found or in use")
    public StatusResponse delete(@PathVariable Long id) {
        log.info("DELETE /api/v1/admin/offering-attributes/{}", id);
        attributeService.delete(id);
        return new StatusResponse("DELETED");
    }
}
