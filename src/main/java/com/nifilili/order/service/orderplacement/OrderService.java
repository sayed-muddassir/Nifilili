package com.nifilili.order.service.orderplacement;

import com.nifilili.order.dto.request.orderplacement.PlaceOrderRequest;
import com.nifilili.order.dto.response.orderplacement.OrderDetailsResponse;
import com.nifilili.order.dto.response.orderplacement.OrderSummaryResponse;
import com.nifilili.order.dto.response.orderplacement.PlaceOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    /**
     * Convert cart into a finalized order.
     * This is a fully atomic operation.
     */
    PlaceOrderResponse placeOrder(PlaceOrderRequest request);

    /**
     * List orders belonging to the logged-in user.
     * Optional status filter.
     */
    Page<OrderSummaryResponse> listMyOrders(String status, Pageable pageable);

    /**
     * Get full order details including items and timeline.
     */
    OrderDetailsResponse getOrderDetails(Long orderId);
}
