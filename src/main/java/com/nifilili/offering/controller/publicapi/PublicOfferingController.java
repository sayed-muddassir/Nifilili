package com.nifilili.offering.controller.publicapi;

import com.nifilili.core.enums.offering.OfferingType;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.dto.response.PublicOfferingResponse;
import com.nifilili.offering.dto.response.PublicOfferingSummary;
import com.nifilili.offering.dto.response.PublicVariantResponse;
import com.nifilili.offering.service.PricingService;
import com.nifilili.offering.service.PublicOfferingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/public/offerings")
@RequiredArgsConstructor
@Tag(name = "Offering Public", description = "Public offering browsing and search endpoints")
public class PublicOfferingController {

    private final PublicOfferingService service;
    private final PricingService pricingService;

    @GetMapping("/{id}")
    @Operation(summary = "Get offering details",
            description = "Returns a published offering with variants, attributes, and discounted price")
    @ApiResponse(responseCode = "200", description = "Offering details returned")
    @ApiResponse(responseCode = "400", description = "Offering not found or not published")
    public PublicOfferingResponse get(@PathVariable Long id) {
        log.info("GET /api/v1/public/offerings/{}", id);

        OfferingEntity entity = service.getPublishedOffering(id);
        BigDecimal discountedPrice = pricingService.resolveOfferingPrice(id);
        List<PublicVariantResponse> variants = service.getVariantsWithDetails(id);

        return new PublicOfferingResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                discountedPrice,
                entity.getImages(),
                variants
        );
    }

    @GetMapping("/search")
    @Operation(summary = "Search offerings",
            description = "Search published offerings by category, type, and price range. All filters are optional and combinable.")
    @ApiResponse(responseCode = "200", description = "Search results returned")
    public Page<PublicOfferingSummary> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) OfferingType type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {
        log.info("GET /api/v1/public/offerings/search - categoryId={}, type={}, minPrice={}, maxPrice={}",
                categoryId, type, minPrice, maxPrice);
        return service.searchOfferings(categoryId, type, minPrice, maxPrice, pageable);
    }

    @GetMapping("/business/{ownerId}")
    @Operation(summary = "Browse by business",
            description = "Returns a paginated list of published offerings from a specific business or owner storefront")
    @ApiResponse(responseCode = "200", description = "Business offerings returned")
    public Page<PublicOfferingSummary> listByBusiness(@PathVariable Long ownerId, Pageable pageable) {
        log.info("GET /api/v1/public/offerings/business/{}", ownerId);
        return service.listByOwner(ownerId, pageable);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Browse by category",
            description = "Returns a paginated list of published offerings within a specific product/service category")
    @ApiResponse(responseCode = "200", description = "Category offerings returned")
    public Page<PublicOfferingSummary> listByCategory(@PathVariable Long categoryId, Pageable pageable) {
        log.info("GET /api/v1/public/offerings/category/{}", categoryId);
        return service.listByCategory(categoryId, pageable);
    }

    @GetMapping("/featured")
    @Operation(summary = "List featured offerings",
            description = "Returns a paginated list of published offerings marked as featured")
    @ApiResponse(responseCode = "200", description = "Featured offerings returned")
    public Page<PublicOfferingSummary> featured(Pageable pageable) {
        log.info("GET /api/v1/public/offerings/featured");
        return service.listFeatured(pageable);
    }
}

