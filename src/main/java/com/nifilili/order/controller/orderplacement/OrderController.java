package com.nifilili.order.controller.orderplacement;

import com.nifilili.order.dto.request.orderplacement.PlaceOrderRequest;
import com.nifilili.order.dto.response.orderplacement.OrderDetailsResponse;
import com.nifilili.order.dto.response.orderplacement.OrderSummaryResponse;
import com.nifilili.order.dto.response.orderplacement.PlaceOrderResponse;
import com.nifilili.order.service.orderplacement.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public PlaceOrderResponse placeOrder(@RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(request);
    }

    @GetMapping
    public Page<OrderSummaryResponse> listOrders(
            @RequestParam(required = false) String status,
            Pageable pageable
    ) {
        return orderService.listMyOrders(status, pageable);
    }

    @GetMapping("/{orderId}")
    public OrderDetailsResponse getOrder(@PathVariable Long orderId) {
        return orderService.getOrderDetails(orderId);
    }
}

