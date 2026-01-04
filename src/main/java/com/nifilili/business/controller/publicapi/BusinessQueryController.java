package com.nifilili.business.controller.publicapi;

import com.nifilili.business.dto.response.BusinessResponse;
import com.nifilili.business.service.BusinessQueryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/businesses")
@RequiredArgsConstructor
public class BusinessQueryController {

    private final BusinessQueryService businessQueryService;

    /* --------------------------------------------
       API 1: GET BUSINESS BY ID
       -------------------------------------------- */
    @Operation(summary = "Get Business by ID",
            description = "Retrieves detailed information about a specific business using its unique identifier.",
            tags = {"Business Query [Public]"}
    )
    @GetMapping("/{businessId}")
    public BusinessResponse getBusinessById(
            @PathVariable Long businessId
    ) {
        return businessQueryService.getBusinessById(businessId);
    }

    /* --------------------------------------------
       API 2: GET ALL BUSINESSES (PAGINATED)
       -------------------------------------------- */
    @Operation(summary = "Get All Businesses",
            description = "Retrieves a paginated list of all businesses.",
            tags = {"Business Query [Public]"}
    )
    @GetMapping
    public Page<BusinessResponse> getAllBusinesses(
            Pageable pageable
    ) {
        return businessQueryService.getAllBusinesses(pageable);
    }
}
