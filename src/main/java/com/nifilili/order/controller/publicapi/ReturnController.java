package com.nifilili.order.controller.publicapi;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.order.dto.request.CreateReturnRequest;
import com.nifilili.order.dto.response.ReturnResponse;
import com.nifilili.order.service.ReturnService;
import io.swagger.v3.oas.annotations.Hidden;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = SwaggerConstants.ORDER_5, description = "Return request endpoints for delivered items")
@Hidden
public class ReturnController {

    private final ReturnService returnService;

    @PostMapping("/{orderItemId}/returns")
    @Operation(summary = "Step 5.1: Create Return Request", description = "Creates a return request for a delivered order item within the return window")
    @ApiResponse(responseCode = "201", description = "Return request created")
    @ApiResponse(responseCode = "400", description = "Item not delivered or return window expired")
    @ApiResponse(responseCode = "404", description = "Order item not found")
    public ResponseEntity<ReturnResponse> createReturn(
            @PathVariable Long orderItemId,
            @Valid @RequestBody CreateReturnRequest request) {
        log.info("POST /api/v1/order-items/{}/returns", orderItemId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(returnService.createReturn(orderItemId, request));
    }
}
