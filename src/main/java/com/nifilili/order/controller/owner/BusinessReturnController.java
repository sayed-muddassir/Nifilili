package com.nifilili.order.controller.owner;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.order.dto.request.UpdateReturnStatusRequest;
import com.nifilili.order.dto.response.ReturnResponse;
import com.nifilili.order.service.BusinessReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/business/returns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = SwaggerConstants.ORDER_8, description = "Business return management endpoints")
public class BusinessReturnController {

    private final BusinessReturnService businessReturnService;

    @PutMapping("/{returnRequestId}/status")
    @Operation(summary = "Update return status", description = "Manages return lifecycle: pickup, receive, inspect, refund or reject")
    @ApiResponse(responseCode = "200", description = "Status updated")
    @ApiResponse(responseCode = "400", description = "Invalid status transition")
    @ApiResponse(responseCode = "404", description = "Return request not found")
    public ResponseEntity<ReturnResponse> updateStatus(@PathVariable Long returnRequestId,
                                                        @Valid @RequestBody UpdateReturnStatusRequest request) {
        log.info("PUT /api/v1/business/returns/{}/status", returnRequestId);
        return ResponseEntity.ok(businessReturnService.updateStatus(returnRequestId, request));
    }
}
