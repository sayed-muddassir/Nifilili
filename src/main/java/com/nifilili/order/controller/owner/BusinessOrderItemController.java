package com.nifilili.order.controller.owner;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.order.dto.request.CancellationDecisionRequest;
import com.nifilili.order.dto.request.RejectOrderItemRequest;
import com.nifilili.order.dto.request.UpdateOrderItemStatusRequest;
import com.nifilili.order.dto.response.CancellationResponse;
import com.nifilili.order.service.CancellationService;
import com.nifilili.order.service.OrderItemLifecycleService;
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
@RequestMapping("/api/v1/business/order-items")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = SwaggerConstants.ORDER_7, description = "Business order item management endpoints")
public class BusinessOrderItemController {

    private final OrderItemLifecycleService orderItemLifecycleService;
    private final CancellationService cancellationService;

    @PutMapping("/{orderItemId}/status")
    @Operation(summary = "Update item status", description = "Updates the status of an order item through its lifecycle")
    @ApiResponse(responseCode = "200", description = "Status updated")
    @ApiResponse(responseCode = "400", description = "Invalid status transition")
    @ApiResponse(responseCode = "404", description = "Order item not found")
    public ResponseEntity<Void> updateStatus(@PathVariable Long orderItemId,
                                              @Valid @RequestBody UpdateOrderItemStatusRequest request) {
        log.info("PUT /api/v1/business/order-items/{}/status", orderItemId);
        orderItemLifecycleService.updateStatus(orderItemId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{orderItemId}/reject")
    @Operation(summary = "Reject order item", description = "Rejects an order item with a mandatory reason")
    @ApiResponse(responseCode = "200", description = "Item rejected")
    @ApiResponse(responseCode = "400", description = "Item cannot be rejected")
    @ApiResponse(responseCode = "404", description = "Order item not found")
    public ResponseEntity<Void> reject(@PathVariable Long orderItemId,
                                        @Valid @RequestBody RejectOrderItemRequest request) {
        log.info("PUT /api/v1/business/order-items/{}/reject", orderItemId);
        orderItemLifecycleService.reject(orderItemId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{orderItemId}/cancellation/decide")
    @Operation(summary = "Decide cancellation", description = "Approves or rejects a pending cancellation request")
    @ApiResponse(responseCode = "200", description = "Decision recorded")
    @ApiResponse(responseCode = "400", description = "No pending cancellation")
    @ApiResponse(responseCode = "404", description = "Cancellation request not found")
    public ResponseEntity<CancellationResponse> decideCancellation(
            @PathVariable Long orderItemId,
            @Valid @RequestBody CancellationDecisionRequest request) {
        log.info("PUT /api/v1/business/order-items/{}/cancellation/decide", orderItemId);
        return ResponseEntity.ok(cancellationService.decide(orderItemId, request));
    }
}
