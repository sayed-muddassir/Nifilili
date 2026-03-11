package com.nifilili.offering.controller.owner;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.dto.request.CreateOfferingRequest;
import com.nifilili.offering.dto.request.UpdateOfferingRequest;
import com.nifilili.offering.dto.response.CreateOfferingResponse;
import com.nifilili.offering.dto.response.OfferingResponse;
import com.nifilili.offering.dto.response.StatusResponse;
import com.nifilili.offering.service.OfferingService;
import io.swagger.v3.oas.annotations.Hidden;
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
@RequestMapping("/api/v1/offerings")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('USER')")
@Tag(name = SwaggerConstants.OFFERING_2, description = "Owner offering lifecycle, variants, and pricing controls.")
@Hidden
public class OfferingController {

    private final OfferingService offeringService;

    @GetMapping
    @Operation(summary = "Step 2.8: List My Offerings",
            description = "Returns paginated list of the authenticated owner's offerings, optionally filtered by status")
    @ApiResponse(responseCode = "200", description = "Offerings page returned")
    public Page<OfferingResponse> listMyOfferings(
            @RequestParam(required = false) OfferingStatus status,
            Pageable pageable) {
        log.info("GET /api/v1/offerings - status={}", status);
        return offeringService.listMyOfferings(status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Step 2.7: Get My Offering", description = "Returns full detail of an offering owned by the authenticated user, including draft and archived")
    @ApiResponse(responseCode = "200", description = "Offering detail returned")
    @ApiResponse(responseCode = "400", description = "Offering not found or not owned by the current user")
    public OfferingResponse getMyOffering(@PathVariable Long id) {
        log.info("GET /api/v1/offerings/{}", id);
        OfferingEntity e = offeringService.getMyOffering(id);
        return new OfferingResponse(
                e.getId(), e.getTitle(), e.getDescription(),
                e.getStatus(), e.getPrice(), e.getImages()
        );
    }

    @PostMapping
    @Operation(summary = "Step 2.1: Create Offering", description = "Creates a new product or service in DRAFT status")
    @ApiResponse(responseCode = "200", description = "Offering created")
    @ApiResponse(responseCode = "400", description = "Invalid category or request")
    public CreateOfferingResponse create(@RequestBody CreateOfferingRequest request) {
        OfferingEntity entity = new OfferingEntity();
        entity.setOwnerType(request.ownerType());
        entity.setOwnerId(request.ownerId());
        entity.setType(request.type());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setSku(request.sku());
        entity.setPrice(request.price());
        entity.setIsDynamicPricing(request.isDynamicPricing());
        entity.setAvailableQuantity(request.availableQuantity());
        entity.setIsFeatured(request.isFeatured());
        entity.setIsB2bEnabled(request.isB2bEnabled());
        entity.setImages(request.images());

        OfferingEntity saved = offeringService.create(entity, request.categoryId());

        return new CreateOfferingResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Step 2.2: Update Offering", description = "Updates an existing offering. Archived offerings cannot be modified.")
    @ApiResponse(responseCode = "200", description = "Offering updated")
    @ApiResponse(responseCode = "400", description = "Offering not found or archived")
    public OfferingResponse update(
            @PathVariable Long id,
            @RequestBody UpdateOfferingRequest r) {

        OfferingEntity updates = new OfferingEntity();
        updates.setTitle(r.title());
        updates.setDescription(r.description());
        updates.setPrice(r.price());
        updates.setAvailableQuantity(r.availableQuantity());
        updates.setIsFeatured(r.isFeatured());
        updates.setIsB2bEnabled(r.isB2bEnabled());
        updates.setImages(r.images());

        OfferingEntity updated = offeringService.update(id, updates);

        return new OfferingResponse(
                updated.getId(),
                updated.getTitle(),
                updated.getDescription(),
                updated.getStatus(),
                updated.getPrice(),
                updated.getImages()
        );
    }

    @PatchMapping("/{id}/publish")
    @Operation(summary = "Step 2.5: Publish Offering", description = "Transitions an offering to PUBLISHED status, making it visible to customers")
    @ApiResponse(responseCode = "200", description = "Offering published")
    @ApiResponse(responseCode = "400", description = "Offering not found")
    public StatusResponse publish(@PathVariable Long id) {
        offeringService.publish(id);
        return new StatusResponse("PUBLISHED");
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Step 2.10: Archive Offering", description = "Transitions an offering to ARCHIVED status, hiding it from customers")
    @ApiResponse(responseCode = "200", description = "Offering archived")
    @ApiResponse(responseCode = "400", description = "Offering not found")
    public StatusResponse archive(@PathVariable Long id) {
        offeringService.archive(id);
        return new StatusResponse("ARCHIVED");
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Step 2.11: Restore Offering", description = "Restores an archived offering back to DRAFT status")
    @ApiResponse(responseCode = "200", description = "Offering restored to draft")
    @ApiResponse(responseCode = "400", description = "Offering not found")
    public StatusResponse restore(@PathVariable Long id) {
        offeringService.restore(id);
        return new StatusResponse("DRAFT");
    }

    @PatchMapping("/{id}/inventory")
    @Operation(summary = "Step 2.9: Update Offering Inventory", description = "Updates the available quantity for an offering")
    @ApiResponse(responseCode = "200", description = "Inventory updated")
    @ApiResponse(responseCode = "400", description = "Offering not found")
    public StatusResponse inventory(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        offeringService.updateInventory(id, quantity);
        return new StatusResponse("UPDATED");
    }
}

