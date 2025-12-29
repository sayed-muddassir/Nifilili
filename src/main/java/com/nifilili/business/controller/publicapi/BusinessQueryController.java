package com.nifilili.business.controller.publicapi;

import com.nifilili.business.dto.response.BusinessResponse;
import com.nifilili.business.service.BusinessQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
public class BusinessQueryController {

    private final BusinessQueryService businessQueryService;

    /* --------------------------------------------
       API 1: GET BUSINESS BY ID
       -------------------------------------------- */
    @GetMapping("/{businessId}")
    public BusinessResponse getBusinessById(
            @PathVariable Long businessId
    ) {
        return businessQueryService.getBusinessById(businessId);
    }

    /* --------------------------------------------
       API 2: GET ALL BUSINESSES (PAGINATED)
       -------------------------------------------- */
    @GetMapping
    public Page<BusinessResponse> getAllBusinesses(
            Pageable pageable
    ) {
        return businessQueryService.getAllBusinesses(pageable);
    }
}
