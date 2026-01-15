package com.nifilili.offering.controller.publicapi;

import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.dto.response.PublicOfferingResponse;
import com.nifilili.offering.dto.response.PublicOfferingSearchResponse;
import com.nifilili.offering.service.pricing.PricingService;
import com.nifilili.offering.service.publicapi.PublicOfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/offerings")
@RequiredArgsConstructor
public class PublicOfferingController {

    private final PublicOfferingService service;
    private final PricingService pricingService;

    @GetMapping("/{id}")
    public PublicOfferingResponse get(@PathVariable Long id) {

        OfferingEntity entity = service.getPublishedOffering(id);

        BigDecimal price = pricingService.resolveOfferingPrice(id);

        return new PublicOfferingResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                price,
                price, // TODO Discounted price can be added here
                entity.getImages(),
                List.of() // TODO Variants can be added here
        );
    }
    @GetMapping("/search")
    public PublicOfferingSearchResponse search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return null;
    }
}

