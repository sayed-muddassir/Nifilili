package com.nifilili.offering.controller.owner;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.dto.request.CreateVariantRequest;
import com.nifilili.offering.dto.request.UpdateVariantRequest;
import com.nifilili.offering.dto.response.StatusResponse;
import com.nifilili.offering.dto.response.VariantResponse;
import com.nifilili.offering.service.OfferingVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/offerings/{offeringId}/variants")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('USER')")
@Tag(name = SwaggerConstants.OFFERING_2, description = "Owner offering lifecycle, variants, and pricing controls.")
public class OfferingVariantController {

    private final OfferingVariantService variantService;

    @PostMapping
    @Operation(summary = "Step 2.3: Create Variant", description = "Creates a new variant for an offering with its own SKU, price, and images")
    @ApiResponse(responseCode = "200", description = "Variant created")
    @ApiResponse(responseCode = "400", description = "Offering not found")
    public VariantResponse create(
            @PathVariable Long offeringId,
            @RequestBody CreateVariantRequest request) {

        OfferingVariantEntity entity = new OfferingVariantEntity();
        entity.setSku(request.sku());
        entity.setPrice(request.price());
        entity.setAvailableQuantity(request.availableQuantity());
        entity.setImages(request.images());

        OfferingVariantEntity saved = variantService.create(offeringId, entity);
        return toResponse(saved);
    }

    @PutMapping("/{variantId}")
    @Operation(summary = "Step 2.4: Update Variant", description = "Updates an existing variant's SKU, price, quantity, and images")
    @ApiResponse(responseCode = "200", description = "Variant updated")
    @ApiResponse(responseCode = "400", description = "Variant not found")
    public VariantResponse update(
            @PathVariable Long offeringId,
            @PathVariable Long variantId,
            @RequestBody UpdateVariantRequest request) {
        log.info("PUT /api/v1/offerings/{}/variants/{}", offeringId, variantId);

        OfferingVariantEntity updates = new OfferingVariantEntity();
        updates.setSku(request.sku());
        updates.setPrice(request.price());
        updates.setAvailableQuantity(request.availableQuantity());
        updates.setImages(request.images());

        return toResponse(variantService.update(variantId, updates));
    }

    @GetMapping
    @Operation(summary = "Step 2.6: List Variants", description = "Returns a paginated list of variants for the specified offering")
    @ApiResponse(responseCode = "200", description = "Variants returned")
    public Page<VariantResponse> list(@PathVariable Long offeringId, Pageable pageable) {
        log.info("GET /api/v1/offerings/{}/variants", offeringId);
        return variantService.listPaginated(offeringId, pageable)
                .map(this::toResponse);
    }

    @PatchMapping("/{variantId}/inventory")
    @Operation(summary = "Step 2.12: Update Variant Inventory", description = "Updates the available quantity for a variant")
    @ApiResponse(responseCode = "200", description = "Variant inventory updated")
    public StatusResponse inventory(
            @PathVariable Long variantId,
            @RequestParam Long quantity) {

        variantService.updateInventory(variantId, quantity);
        return new StatusResponse("UPDATED");
    }

    @PatchMapping("/{variantId}/deactivate")
    @Operation(summary = "Step 2.13: Deactivate Variant",
            description = "Soft-deletes a variant by setting its status to INACTIVE")
    @ApiResponse(responseCode = "200", description = "Variant deactivated")
    @ApiResponse(responseCode = "400", description = "Variant not found")
    public StatusResponse deactivate(@PathVariable Long offeringId, @PathVariable Long variantId) {
        log.info("PATCH /api/v1/offerings/{}/variants/{}/deactivate", offeringId, variantId);
        variantService.deactivate(variantId);
        return new StatusResponse("INACTIVE");
    }

    private VariantResponse toResponse(OfferingVariantEntity entity) {
        return new VariantResponse(
                entity.getId(),
                entity.getSku(),
                entity.getPrice(),
                entity.getAvailableQuantity(),
                entity.getStatus(),
                entity.getImages()
        );
    }
}
