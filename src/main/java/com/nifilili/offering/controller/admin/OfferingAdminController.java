package com.nifilili.offering.controller.admin;

import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.dto.request.AdminStatusChangeRequest;
import com.nifilili.offering.dto.response.OfferingResponse;
import com.nifilili.offering.service.OfferingAdminService;
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
@RequestMapping("/api/v1/admin/offerings")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ADMIN')")
@Tag(name = "Offering Admin", description = "Admin oversight and moderation of all platform offerings")
public class OfferingAdminController {

    private final OfferingAdminService offeringAdminService;

    @GetMapping
    @Operation(summary = "List all offerings",
            description = "Returns a paginated list of all offerings across all owners, optionally filtered by status")
    @ApiResponse(responseCode = "200", description = "Offerings returned")
    public Page<OfferingResponse> listAll(
            @RequestParam(required = false) OfferingStatus status,
            Pageable pageable) {
        log.info("GET /api/v1/admin/offerings - status={}", status);
        return offeringAdminService.listAll(status, pageable)
                .map(e -> new OfferingResponse(
                        e.getId(), e.getTitle(), e.getDescription(),
                        e.getStatus(), e.getPrice(), e.getImages()
                ));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change offering status",
            description = "Admin force-changes an offering's status (e.g., ARCHIVED to suspend a problematic offering)")
    @ApiResponse(responseCode = "200", description = "Status changed")
    @ApiResponse(responseCode = "400", description = "Offering not found")
    public OfferingResponse changeStatus(
            @PathVariable Long id,
            @RequestBody AdminStatusChangeRequest request) {
        log.info("PATCH /api/v1/admin/offerings/{}/status -> {}", id, request.status());
        OfferingEntity updated = offeringAdminService.changeStatus(id, request.status());
        return new OfferingResponse(
                updated.getId(), updated.getTitle(), updated.getDescription(),
                updated.getStatus(), updated.getPrice(), updated.getImages()
        );
    }
}
