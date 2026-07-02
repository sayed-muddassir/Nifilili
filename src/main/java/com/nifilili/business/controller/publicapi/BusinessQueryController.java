package com.nifilili.business.controller.publicapi;

import com.nifilili.business.dto.response.BusinessResponse;
import com.nifilili.business.service.BusinessQueryService;
import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.core.enums.business.BusinessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/businesses")
@RequiredArgsConstructor
@Slf4j
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

    @Operation(
        summary = "Get Top N Businesses by Vertical & Municipality",
        description = "Returns top N published businesses for a given vertical and municipality, sorted by average rating (highest first).")
    @GetMapping("/top")
    public ResponseEntity<List<BusinessResponse>> getTopByVerticalAndMunicipality(
        @Parameter(description = "Vertical ID (e.g., 1000 for Health)", required = true, example = "1000")
        @RequestParam Long verticalId,

        @Parameter(description = "Municipality ID (e.g., 1000 for Kathmandu)", required = true, example = "1000")
        @RequestParam Long municipalityId,

        @Parameter(description = "Number of top businesses to return (default: 5, min: 1, max: 100)", example = "5")
        @RequestParam(defaultValue = "5") int top
    ) {
        log.info("Fetching top {} businesses for verticalId={}, municipalityId={}", top, verticalId, municipalityId);

        try {
            List<BusinessResponse> businesses = businessQueryService.getTopNByVerticalAndMunicipality(
                verticalId, municipalityId, top
            );

            if (businesses.isEmpty()) {
                log.warn("No businesses found for verticalId={}, municipalityId={}", verticalId, municipalityId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(businesses);
            }

            log.info("Successfully retrieved {} businesses for verticalId={}, municipalityId={}", businesses.size(), verticalId, municipalityId);
            return ResponseEntity.ok(businesses);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching top businesses for verticalId={}, municipalityId={}", verticalId, municipalityId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
