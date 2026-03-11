package com.nifilili.order.controller.publicapi;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.core.enums.order.OrderStatus;
import com.nifilili.order.dto.request.PlaceOrderRequest;
import com.nifilili.order.dto.response.OrderDetailsResponse;
import com.nifilili.order.dto.response.OrderSummaryResponse;
import com.nifilili.order.dto.response.PlaceOrderResponse;
import com.nifilili.order.service.OrderPlacementService;
import com.nifilili.order.service.OrderQueryService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = SwaggerConstants.ORDER_3, description = "Order placement and query endpoints")
@Hidden
public class OrderController {

    private final OrderPlacementService orderPlacementService;
    private final OrderQueryService orderQueryService;

    @PostMapping
    @Operation(summary = "Step 3.1: Place Order", description = "Places an order from the current cart with delivery address and payment type")
    @ApiResponse(responseCode = "201", description = "Order placed")
    @ApiResponse(responseCode = "400", description = "Cart empty or invalid request")
    @ApiResponse(responseCode = "404", description = "Payment type not found")
    public ResponseEntity<PlaceOrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        log.info("POST /api/v1/orders");
        return ResponseEntity.status(HttpStatus.CREATED).body(orderPlacementService.placeOrder(request));
    }

    @GetMapping
    @Operation(summary = "Step 3.2: List My Orders", description = "Returns paginated list of the authenticated user's orders")
    @ApiResponse(responseCode = "200", description = "Orders returned")
    public ResponseEntity<Page<OrderSummaryResponse>> listOrders(
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {
        log.info("GET /api/v1/orders - status={}", status);
        return ResponseEntity.ok(orderQueryService.listMyOrders(status, pageable));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Step 3.3: Get Order Details", description = "Returns full order details with items, timeline, and payment")
    @ApiResponse(responseCode = "200", description = "Order details returned")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<OrderDetailsResponse> getOrderDetails(@PathVariable Long orderId) {
        log.info("GET /api/v1/orders/{}", orderId);
        return ResponseEntity.ok(orderQueryService.getOrderDetails(orderId));
    }
}
