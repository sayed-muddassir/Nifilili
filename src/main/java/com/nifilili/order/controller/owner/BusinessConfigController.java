package com.nifilili.order.controller.owner;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.dto.request.UpdateBusinessConfigRequest;
import com.nifilili.order.dto.response.BusinessConfigResponse;
import com.nifilili.order.service.BusinessConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/business/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = SwaggerConstants.ORDER_11, description = "Business configuration management (tax rate, delivery charge, etc.)")
public class BusinessConfigController {

    private final BusinessConfigService businessConfigService;

    @GetMapping
    @Operation(summary = "Get configs", description = "Returns all configuration key-value pairs for the business")
    @ApiResponse(responseCode = "200", description = "Configs returned")
    public ResponseEntity<List<BusinessConfigResponse>> getConfigs() {
        Long businessId = SecurityUtil.getCurrentUserId();
        log.info("GET /api/v1/business/config - businessId={}", businessId);
        return ResponseEntity.ok(businessConfigService.getConfigs(businessId));
    }

    @PutMapping
    @Operation(summary = "Upsert config", description = "Creates or updates a configuration key-value pair")
    @ApiResponse(responseCode = "200", description = "Config saved")
    @ApiResponse(responseCode = "400", description = "Invalid config data")
    public ResponseEntity<BusinessConfigResponse> upsertConfig(
            @Valid @RequestBody UpdateBusinessConfigRequest request) {
        Long businessId = SecurityUtil.getCurrentUserId();
        log.info("PUT /api/v1/business/config - businessId={}, key={}", businessId, request.getConfigKey());
        return ResponseEntity.ok(businessConfigService.upsertConfig(businessId, request));
    }
}
