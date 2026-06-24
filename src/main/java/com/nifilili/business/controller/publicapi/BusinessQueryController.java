package com.nifilili.business.controller.publicapi;

import com.nifilili.business.dto.response.BusinessResponse;
import com.nifilili.business.service.BusinessQueryService;
import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.core.enums.business.BusinessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/businesses")
@RequiredArgsConstructor
@Tag(name = SwaggerConstants.BUSINESS_3)
public class BusinessQueryController {

    private final BusinessQueryService businessQueryService;

    @Operation(summary = "Step 1: List Published Businesses", description = "Returns paginated published businesses with sections and attributes.")
    @GetMapping
    public Page<BusinessResponse> getAllBusinesses(@ParameterObject Pageable pageable) {
        return businessQueryService.getAllBusinessesByStatus(pageable, BusinessStatus.PUBLISHED);
    }

    @Operation(summary = "Step 2: Get Business Details", description = "Returns full details for one published business id.")
    @GetMapping("/{businessId}")
    public BusinessResponse getBusinessById(@PathVariable Long businessId) {
        return businessQueryService.getBusinessById(businessId);
    }
}
