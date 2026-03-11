package com.nifilili.business.controller.admin;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nifilili.business.dto.request.CreateDistrictRequest;
import com.nifilili.business.dto.request.CreateMunicipalityRequest;
import com.nifilili.business.dto.request.CreateProvinceRequest;
import com.nifilili.business.dto.response.DistrictResponse;
import com.nifilili.business.dto.response.MunicipalityResponse;
import com.nifilili.business.dto.response.ProvinceResponse;
import com.nifilili.business.service.DistrictAdminService;
import com.nifilili.business.service.MunicipalityAdminService;
import com.nifilili.business.service.ProvinceAdminService;
import com.nifilili.core.constants.SwaggerConstants;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/business-locations")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ADMIN')")
@Tag(name = SwaggerConstants.BUSINESS_1, description = "Admin management of province, district, and municipality masters.")
public class BusinessLocationAdminController {

    private final ProvinceAdminService provinceAdminService;
    private final DistrictAdminService districtAdminService;
    private final MunicipalityAdminService municipalityAdminService;

    @Operation(summary = "Create Province", description = "Creates a new province master record.")
    @ApiResponse(responseCode = "201", description = "Province created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @PostMapping("/provinces")
    public ResponseEntity<ProvinceResponse> createProvince(@Valid @RequestBody CreateProvinceRequest request) {
        log.info("POST /api/v1/admin/business-locations/provinces");
        ProvinceResponse response = provinceAdminService.create(request);
        log.info("Created province id={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "List Provinces", description = "Returns all province master records.")
    @ApiResponse(responseCode = "200", description = "Province list returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @GetMapping("/provinces")
    public ResponseEntity<List<ProvinceResponse>> getProvinces() {
        log.info("GET /api/v1/admin/business-locations/provinces");
        List<ProvinceResponse> response = provinceAdminService.getAll();
        log.info("Returning {} provinces", response.size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Province", description = "Returns one province master record by id.")
    @ApiResponse(responseCode = "200", description = "Province returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Province not found")
    @GetMapping("/provinces/{provinceId}")
    public ResponseEntity<ProvinceResponse> getProvince(@PathVariable Long provinceId) {
        log.info("GET /api/v1/admin/business-locations/provinces/{}", provinceId);
        ProvinceResponse response = provinceAdminService.getById(provinceId);
        log.info("Returning province id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Province", description = "Updates an existing province master record.")
    @ApiResponse(responseCode = "200", description = "Province updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Province not found")
    @PutMapping("/provinces/{provinceId}")
    public ResponseEntity<ProvinceResponse> updateProvince(@PathVariable Long provinceId,
                                                           @Valid @RequestBody CreateProvinceRequest request) {
        log.info("PUT /api/v1/admin/business-locations/provinces/{}", provinceId);
        ProvinceResponse response = provinceAdminService.update(provinceId, request);
        log.info("Updated province id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Province", description = "Deletes a province that has no child districts.")
    @ApiResponse(responseCode = "204", description = "Province deleted successfully")
    @ApiResponse(responseCode = "400", description = "Province has child districts")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Province not found")
    @DeleteMapping("/provinces/{provinceId}")
    public ResponseEntity<Void> deleteProvince(@PathVariable Long provinceId) {
        log.info("DELETE /api/v1/admin/business-locations/provinces/{}", provinceId);
        provinceAdminService.delete(provinceId);
        log.info("Deleted province id={}", provinceId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create District", description = "Creates a new district master record under a province.")
    @ApiResponse(responseCode = "201", description = "District created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Province not found")
    @PostMapping("/districts")
    public ResponseEntity<DistrictResponse> createDistrict(@Valid @RequestBody CreateDistrictRequest request) {
        log.info("POST /api/v1/admin/business-locations/districts");
        DistrictResponse response = districtAdminService.create(request);
        log.info("Created district id={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "List Districts by Province",
            description = "Returns all district master records under the specified province.")
    @ApiResponse(responseCode = "200", description = "District list returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Province not found")
    @GetMapping("/provinces/{provinceId}/districts")
    public ResponseEntity<List<DistrictResponse>> getDistrictsByProvince(@PathVariable Long provinceId) {
        log.info("GET /api/v1/admin/business-locations/provinces/{}/districts", provinceId);
        List<DistrictResponse> response = districtAdminService.getByProvince(provinceId);
        log.info("Returning {} districts for provinceId={}", response.size(), provinceId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get District", description = "Returns one district master record by id.")
    @ApiResponse(responseCode = "200", description = "District returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "District not found")
    @GetMapping("/districts/{districtId}")
    public ResponseEntity<DistrictResponse> getDistrict(@PathVariable Long districtId) {
        log.info("GET /api/v1/admin/business-locations/districts/{}", districtId);
        DistrictResponse response = districtAdminService.getById(districtId);
        log.info("Returning district id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update District", description = "Updates an existing district master record.")
    @ApiResponse(responseCode = "200", description = "District updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "District or province not found")
    @PutMapping("/districts/{districtId}")
    public ResponseEntity<DistrictResponse> updateDistrict(@PathVariable Long districtId,
                                                           @Valid @RequestBody CreateDistrictRequest request) {
        log.info("PUT /api/v1/admin/business-locations/districts/{}", districtId);
        DistrictResponse response = districtAdminService.update(districtId, request);
        log.info("Updated district id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete District", description = "Deletes a district that has no child municipalities.")
    @ApiResponse(responseCode = "204", description = "District deleted successfully")
    @ApiResponse(responseCode = "400", description = "District has child municipalities")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "District not found")
    @DeleteMapping("/districts/{districtId}")
    public ResponseEntity<Void> deleteDistrict(@PathVariable Long districtId) {
        log.info("DELETE /api/v1/admin/business-locations/districts/{}", districtId);
        districtAdminService.delete(districtId);
        log.info("Deleted district id={}", districtId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create Municipality", description = "Creates a new municipality master record under a district.")
    @ApiResponse(responseCode = "201", description = "Municipality created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "District not found")
    @PostMapping("/municipalities")
    public ResponseEntity<MunicipalityResponse> createMunicipality(@Valid @RequestBody CreateMunicipalityRequest request) {
        log.info("POST /api/v1/admin/business-locations/municipalities");
        MunicipalityResponse response = municipalityAdminService.create(request);
        log.info("Created municipality id={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "List Municipalities by District",
            description = "Returns all municipality master records under the specified district.")
    @ApiResponse(responseCode = "200", description = "Municipality list returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "District not found")
    @GetMapping("/districts/{districtId}/municipalities")
    public ResponseEntity<List<MunicipalityResponse>> getMunicipalitiesByDistrict(@PathVariable Long districtId) {
        log.info("GET /api/v1/admin/business-locations/districts/{}/municipalities", districtId);
        List<MunicipalityResponse> response = municipalityAdminService.getByDistrict(districtId);
        log.info("Returning {} municipalities for districtId={}", response.size(), districtId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Municipality", description = "Returns one municipality master record by id.")
    @ApiResponse(responseCode = "200", description = "Municipality returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Municipality not found")
    @GetMapping("/municipalities/{municipalityId}")
    public ResponseEntity<MunicipalityResponse> getMunicipality(@PathVariable Long municipalityId) {
        log.info("GET /api/v1/admin/business-locations/municipalities/{}", municipalityId);
        MunicipalityResponse response = municipalityAdminService.getById(municipalityId);
        log.info("Returning municipality id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Municipality", description = "Updates an existing municipality master record.")
    @ApiResponse(responseCode = "200", description = "Municipality updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Municipality or district not found")
    @PutMapping("/municipalities/{municipalityId}")
    public ResponseEntity<MunicipalityResponse> updateMunicipality(@PathVariable Long municipalityId,
                                                                   @Valid @RequestBody CreateMunicipalityRequest request) {
        log.info("PUT /api/v1/admin/business-locations/municipalities/{}", municipalityId);
        MunicipalityResponse response = municipalityAdminService.update(municipalityId, request);
        log.info("Updated municipality id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Municipality",
            description = "Deletes a municipality that is not referenced by any business record.")
    @ApiResponse(responseCode = "204", description = "Municipality deleted successfully")
    @ApiResponse(responseCode = "400", description = "Municipality is still referenced by businesses")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Municipality not found")
    @DeleteMapping("/municipalities/{municipalityId}")
    public ResponseEntity<Void> deleteMunicipality(@PathVariable Long municipalityId) {
        log.info("DELETE /api/v1/admin/business-locations/municipalities/{}", municipalityId);
        municipalityAdminService.delete(municipalityId);
        log.info("Deleted municipality id={}", municipalityId);
        return ResponseEntity.noContent().build();
    }
}
