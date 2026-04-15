package com.nifilili.business.controller.owner;

import com.nifilili.business.dto.response.DistrictResponse;
import com.nifilili.business.dto.response.MunicipalityResponse;
import com.nifilili.business.dto.response.ProvinceResponse;
import com.nifilili.business.service.DistrictAdminService;
import com.nifilili.business.service.MunicipalityAdminService;
import com.nifilili.business.service.ProvinceAdminService;
import com.nifilili.core.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/business-locations")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('USER')")
@Tag(name = SwaggerConstants.BUSINESS_4)
public class BusinessLocationOwnerController {

    private final ProvinceAdminService provinceAdminService;
    private final DistrictAdminService districtAdminService;
    private final MunicipalityAdminService municipalityAdminService;

    @Operation(summary = "Step 1: List Provinces",
            description = "Returns all province master records. Use this after province creation to confirm available parent records.")
    @ApiResponse(responseCode = "200", description = "Province list returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @GetMapping("/provinces")
    public ResponseEntity<List<ProvinceResponse>> getProvinces() {
        log.info("GET /api/v1/business-locations/provinces");
        List<ProvinceResponse> response = provinceAdminService.getAll();
        log.info("Returning {} provinces", response.size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Step 2: Get Province",
            description = "Returns one province master record by id. Use this when validating a selected province before creating districts.")
    @ApiResponse(responseCode = "200", description = "Province returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Province not found")
    @GetMapping("/provinces/{provinceId}")
    public ResponseEntity<ProvinceResponse> getProvince(@PathVariable Long provinceId) {
        log.info("GET /api/v1/business-locations/provinces/{}", provinceId);
        ProvinceResponse response = provinceAdminService.getById(provinceId);
        log.info("Returning province id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Step 3: List Districts by Province",
            description = "Returns all district master records under the specified province. Use this after district creation to confirm available parent records for municipalities.")
    @ApiResponse(responseCode = "200", description = "District list returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Province not found")
    @GetMapping("/provinces/{provinceId}/districts")
    public ResponseEntity<List<DistrictResponse>> getDistrictsByProvince(@PathVariable Long provinceId) {
        log.info("GET /api/v1/business-locations/provinces/{}/districts", provinceId);
        List<DistrictResponse> response = districtAdminService.getByProvince(provinceId);
        log.info("Returning {} districts for provinceId={}", response.size(), provinceId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Step 4: Get District",
            description = "Returns one district master record by id. Use this when validating a selected district before creating municipalities.")
    @ApiResponse(responseCode = "200", description = "District returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "District not found")
    @GetMapping("/districts/{districtId}")
    public ResponseEntity<DistrictResponse> getDistrict(@PathVariable Long districtId) {
        log.info("GET /api/v1/business-locations/districts/{}", districtId);
        DistrictResponse response = districtAdminService.getById(districtId);
        log.info("Returning district id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Step 5: List Municipalities by District",
            description = "Returns all municipality master records under the specified district. Use this to confirm the municipality ids that business onboarding should reference.")
    @ApiResponse(responseCode = "200", description = "Municipality list returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "District not found")
    @GetMapping("/districts/{districtId}/municipalities")
    public ResponseEntity<List<MunicipalityResponse>> getMunicipalitiesByDistrict(@PathVariable Long districtId) {
        log.info("GET /api/v1/business-locations/districts/{}/municipalities", districtId);
        List<MunicipalityResponse> response = municipalityAdminService.getByDistrict(districtId);
        log.info("Returning {} municipalities for districtId={}", response.size(), districtId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Step 6: Get Municipality",
            description = "Returns one municipality master record by id. Use this when validating the final location record before assigning it to a business.")
    @ApiResponse(responseCode = "200", description = "Municipality returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Municipality not found")
    @GetMapping("/municipalities/{municipalityId}")
    public ResponseEntity<MunicipalityResponse> getMunicipality(@PathVariable Long municipalityId) {
        log.info("GET /api/v1/business-locations/municipalities/{}", municipalityId);
        MunicipalityResponse response = municipalityAdminService.getById(municipalityId);
        log.info("Returning municipality id={}", response.getId());
        return ResponseEntity.ok(response);
    }


}
