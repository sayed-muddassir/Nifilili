package com.nifilili.config.controller.admin;

import com.nifilili.config.dto.request.CreateVerticalRequest;
import com.nifilili.config.dto.response.VerticalResponse;
import com.nifilili.config.service.VerticalService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/config/verticals")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ADMIN')")
public class VerticalAdminController {

    private final VerticalService service;

    @Operation(summary = "Create Vertical",
            description = "Creates a new vertical in the system.",
            tags = {"Vertical Management [Admin]"}
    )
    @PostMapping
    public VerticalResponse create(@RequestBody CreateVerticalRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Get All Active Verticals",
            description = "Retrieves all active verticals in the system.",
            tags = {"Vertical Management [Admin]"}
    )
    @GetMapping
    public List<VerticalResponse> getAllActive() {
        return service.getAllActive();
    }
}
