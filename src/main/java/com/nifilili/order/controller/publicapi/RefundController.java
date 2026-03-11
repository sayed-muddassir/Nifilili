package com.nifilili.order.controller.publicapi;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.order.dto.request.CreateRefundRequest;
import com.nifilili.order.dto.response.RefundResponse;
import com.nifilili.order.service.RefundService;
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
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = SwaggerConstants.ORDER_6, description = "Refund request endpoints")
@Hidden
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/{orderId}/refunds")
    @Operation(summary = "Step 6.1: Create Refund Request", description = "Creates a refund request with bank account details")
    @ApiResponse(responseCode = "201", description = "Refund request created")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<RefundResponse> createRefund(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateRefundRequest request) {
        log.info("POST /api/v1/orders/{}/refunds", orderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(refundService.createRefund(orderId, request));
    }
}
