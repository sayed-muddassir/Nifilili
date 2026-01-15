package com.nifilili.offering.controller.owner;

import com.nifilili.offering.domain.OfferingDiscountEntity;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.dto.request.CreateDiscountRequest;
import com.nifilili.offering.dto.response.DiscountResponse;
import com.nifilili.offering.service.owner.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping
    public DiscountResponse create(@RequestBody CreateDiscountRequest request) {

        OfferingDiscountEntity saved = discountService.create(request);

        return new DiscountResponse(saved.getId(), saved.getStatus());
    }
}

