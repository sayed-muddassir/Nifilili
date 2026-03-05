package com.nifilili.order.controller.publicapi;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.order.dto.request.CancellationRequest;
import com.nifilili.order.dto.response.CancellationResponse;
import com.nifilili.order.service.CancellationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = SwaggerConstants.ORDER_4, description = "Order cancellation request endpoints")
public class CancellationController {

    private final CancellationService cancellationService;

    @PostMapping("/{orderId}/cancellations")
    @Operation(summary = "Request cancellation", description = "Requests cancellation for one or more order items")
    @ApiResponse(responseCode = "201", description = "Cancellation requested")
    @ApiResponse(responseCode = "400", description = "Item not in cancellable state")
    @ApiResponse(responseCode = "404", description = "Order or item not found")
    public ResponseEntity<List<CancellationResponse>> requestCancellation(
            @PathVariable Long orderId,
            @Valid @RequestBody CancellationRequest request) {
        log.info("POST /api/v1/orders/{}/cancellations", orderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cancellationService.requestCancellation(orderId, request));
    }
}
