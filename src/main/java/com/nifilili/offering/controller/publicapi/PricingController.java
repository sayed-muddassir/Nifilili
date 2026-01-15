package com.nifilili.offering.controller.publicapi;

import com.nifilili.offering.dto.response.PricingResponse;
import com.nifilili.offering.service.pricing.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @GetMapping("/variants/{variantId}")
    public PricingResponse variantPrice(@PathVariable Long variantId) {

        return pricingService.getVariantPriceDetails(variantId);
    }
}


