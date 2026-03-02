package com.nifilili.offering.controller.owner;

import com.nifilili.offering.dto.request.AssignVariantAttributesRequest;
import com.nifilili.offering.dto.request.UpdateVariantAttributeRequest;
import com.nifilili.offering.dto.response.StatusResponse;
import com.nifilili.offering.dto.response.VariantAttributeDetailResponse;
import com.nifilili.offering.dto.response.VariantAttributeResponse;
import com.nifilili.offering.service.VariantAttributeService;
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
@RequestMapping("/api/v1/variants/{variantId}/attributes")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('USER')")
@Tag(name = "Variant Attribute Owner", description = "Attribute assignment for product variants")
public class VariantAttributeController {

    private final VariantAttributeService service;

    @PostMapping
    @Operation(summary = "Assign attributes to variant",
            description = "Assigns attribute values (e.g., Color=Red, Size=M) to a product variant")
    @ApiResponse(responseCode = "200", description = "Attributes assigned")
    @ApiResponse(responseCode = "400", description = "Invalid attribute or variant not found")
    public VariantAttributeResponse assign(
            @PathVariable Long variantId,
            @RequestBody AssignVariantAttributesRequest request) {

        service.assignAttributes(variantId, request);

        return new VariantAttributeResponse(variantId, true);
    }

    @GetMapping
    @Operation(summary = "List variant attributes",
            description = "Returns all attribute key-value pairs assigned to the specified variant")
    @ApiResponse(responseCode = "200", description = "Attributes returned")
    public List<VariantAttributeDetailResponse> list(@PathVariable Long variantId) {
        log.info("GET /api/v1/variants/{}/attributes", variantId);
        return service.listAttributes(variantId);
    }

    @PatchMapping("/{attributeId}")
    @Operation(summary = "Update variant attribute value",
            description = "Changes the value of an existing variant attribute, validating against predefined options if applicable")
    @ApiResponse(responseCode = "200", description = "Attribute value updated")
    @ApiResponse(responseCode = "400", description = "Attribute not found or value not allowed")
    public StatusResponse update(
            @PathVariable Long variantId,
            @PathVariable Long attributeId,
            @RequestBody UpdateVariantAttributeRequest request) {
        log.info("PATCH /api/v1/variants/{}/attributes/{}", variantId, attributeId);
        service.updateAttribute(attributeId, request.attributeValue());
        return new StatusResponse("UPDATED");
    }

    @DeleteMapping("/{attributeId}")
    @Operation(summary = "Delete variant attribute",
            description = "Removes an attribute assignment from the variant")
    @ApiResponse(responseCode = "200", description = "Attribute deleted")
    @ApiResponse(responseCode = "400", description = "Attribute not found")
    public StatusResponse delete(@PathVariable Long variantId, @PathVariable Long attributeId) {
        log.info("DELETE /api/v1/variants/{}/attributes/{}", variantId, attributeId);
        service.deleteAttribute(attributeId);
        return new StatusResponse("DELETED");
    }
}

