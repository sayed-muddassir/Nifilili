package com.nifilili.offering.controller.publicapi;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.offering.dto.response.PricingResponse;
import com.nifilili.offering.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/public/pricing")
@RequiredArgsConstructor
@Tag(name = SwaggerConstants.OFFERING_3, description = "Public offering discovery and pricing endpoints.")
public class PricingController {

    private final PricingService pricingService;

    @GetMapping("/variants/{variantId}")
    @Operation(summary = "Step 3.6: Get Variant Pricing",
            description = "Returns base price, active discount, and final price for a variant")
    @ApiResponse(responseCode = "200", description = "Pricing details returned")
    @ApiResponse(responseCode = "400", description = "Variant not found")
    public PricingResponse variantPrice(@PathVariable Long variantId) {

        return pricingService.getVariantPriceDetails(variantId);
    }
}

