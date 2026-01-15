package com.nifilili.offering.controller.owner;

import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.dto.request.CreateOfferingRequest;
import com.nifilili.offering.dto.request.UpdateOfferingRequest;
import com.nifilili.offering.dto.response.CreateOfferingResponse;
import com.nifilili.offering.dto.response.OfferingResponse;
import com.nifilili.offering.dto.response.StatusResponse;
import com.nifilili.offering.service.owner.OfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/offerings")
@RequiredArgsConstructor
public class OfferingController {

    private final OfferingService offeringService;

    @PostMapping
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
    public StatusResponse publish(@PathVariable Long id) {
        offeringService.publish(id);
        return new StatusResponse("PUBLISHED");
    }

    @PatchMapping("/{id}/archive")
    public StatusResponse archive(@PathVariable Long id) {
        offeringService.archive(id);
        return new StatusResponse("ARCHIVED");
    }

    @PatchMapping("/{id}/restore")
    public StatusResponse restore(@PathVariable Long id) {
        offeringService.restore(id);
        return new StatusResponse("DRAFT");
    }

    @PatchMapping("/{id}/inventory")
    public StatusResponse inventory(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        offeringService.updateInventory(id, quantity);
        return new StatusResponse("UPDATED");
    }
}


