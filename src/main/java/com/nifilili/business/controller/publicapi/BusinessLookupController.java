package com.nifilili.business.controller.publicapi;

import com.nifilili.business.dto.response.CategoryResponse;
import com.nifilili.business.dto.response.MunicipalityResponse;
import com.nifilili.business.dto.response.VerticalResponse;
import com.nifilili.business.service.CategoryDefinitionService;
import com.nifilili.business.service.MunicipalityPublicService;
import com.nifilili.business.service.VerticalDefinitionService;
import com.nifilili.core.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/config")
@RequiredArgsConstructor
@Tag(name = SwaggerConstants.BUSINESS_6)
public class BusinessLookupController {


    private final VerticalDefinitionService verticalDefinitionService;
    private final CategoryDefinitionService categoryDefinitionService;
    private final MunicipalityPublicService municipalityPublicService;

    @Operation(summary = "Step 1.0: Get Active Verticals", description = "Loads active verticals to start onboarding.")
    @GetMapping("/verticals/active")
    public List<VerticalResponse> getAllActiveVerticals() {
        return verticalDefinitionService.getAllActive();
    }

    @Operation(summary = "Step 2.0: Get Categories by Vertical", description = "Loads categories under selected vertical.")
    @GetMapping("/vertical/{verticalId}/categories")
    public List<CategoryResponse> getCategoriesByVertical(@PathVariable Long verticalId) {
        return categoryDefinitionService.getByVertical(verticalId);
    }

    @Operation(summary = "Step 2.1: Get all available Categories", description = "Loads all categories available.")
    @GetMapping("/vertical/all/categories")
    public List<CategoryResponse> getAllCategories() {
        return categoryDefinitionService.getAll();
    }

    @Operation(summary = "Step 3.0: Get all Municipalities", description = "Loads all municipalities available.")
    @GetMapping("/municipalities")
    public List<MunicipalityResponse> getAllMunicipalities() {
        return municipalityPublicService.getAll();
    }
}
