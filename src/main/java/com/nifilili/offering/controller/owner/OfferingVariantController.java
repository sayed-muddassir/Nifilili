package com.nifilili.offering.controller.owner;

import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.dto.request.CreateVariantRequest;
import com.nifilili.offering.dto.response.StatusResponse;
import com.nifilili.offering.dto.response.VariantResponse;
import com.nifilili.offering.service.owner.OfferingVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/offerings/{offeringId}/variants")
@RequiredArgsConstructor
public class OfferingVariantController {

    private final OfferingVariantService variantService;

    @PostMapping
    public VariantResponse create(
            @PathVariable Long offeringId,
            @RequestBody CreateVariantRequest request) {

        OfferingVariantEntity entity = new OfferingVariantEntity();
        entity.setSku(request.sku());
        entity.setPrice(request.price());
        entity.setAvailableQuantity(request.availableQuantity());
        entity.setImages(request.images());

        OfferingVariantEntity saved =
                variantService.create(offeringId, entity);

        return toResponse(saved);
    }

    @GetMapping
    public List<VariantResponse> list(@PathVariable Long offeringId) {
        return variantService.list(offeringId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PatchMapping("/{variantId}/inventory")
    public StatusResponse inventory(
            @PathVariable Long variantId,
            @RequestParam Long quantity) {

        variantService.updateInventory(variantId, quantity);
        return new StatusResponse("UPDATED");
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


