package com.nifilili.offering.controller.owner;

import com.nifilili.offering.domain.OfferingDiscountEntity;
import com.nifilili.offering.dto.request.CreateDiscountRequest;
import com.nifilili.offering.dto.response.DiscountDetailResponse;
import com.nifilili.offering.dto.response.DiscountResponse;
import com.nifilili.offering.dto.response.StatusResponse;
import com.nifilili.offering.service.DiscountService;
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
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('USER')")
@Tag(name = "Discount Owner", description = "Discount management for offerings and variants")
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping
    @Operation(summary = "Create discount",
            description = "Creates a time-bound percentage or flat discount for an offering or specific variant")
    @ApiResponse(responseCode = "200", description = "Discount created")
    @ApiResponse(responseCode = "400", description = "Invalid offering or variant")
    public DiscountResponse create(@RequestBody CreateDiscountRequest request) {

        OfferingDiscountEntity saved = discountService.create(request);

        return new DiscountResponse(saved.getId(), saved.getStatus());
    }

    @GetMapping("/offering/{offeringId}")
    @Operation(summary = "List discounts by offering",
            description = "Returns a paginated list of all discounts (active and inactive) for the specified offering")
    @ApiResponse(responseCode = "200", description = "Discount list returned")
    public Page<DiscountDetailResponse> listByOffering(@PathVariable Long offeringId, Pageable pageable) {
        log.info("GET /api/v1/discounts/offering/{}", offeringId);
        return discountService.listByOffering(offeringId, pageable);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate discount",
            description = "Sets the discount status to INACTIVE, effectively ending the discount")
    @ApiResponse(responseCode = "200", description = "Discount deactivated")
    @ApiResponse(responseCode = "400", description = "Discount not found")
    public StatusResponse deactivate(@PathVariable Long id) {
        log.info("PATCH /api/v1/discounts/{}/deactivate", id);
        discountService.deactivate(id);
        return new StatusResponse("INACTIVE");
    }
}

