package com.nifilili.order.service;

import com.nifilili.core.enums.order.OrderStatus;
import com.nifilili.order.dto.response.OrderDetailsResponse;
import com.nifilili.order.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderQueryService {

    /**
     * Returns a paginated list of the authenticated user's orders, optionally filtered by status.
     *
     * @param status   optional order status filter
     * @param pageable pagination parameters
     * @return page of order summaries
     */
    Page<OrderSummaryResponse> listMyOrders(OrderStatus status, Pageable pageable);

    /**
     * Returns full order details including items with timeline and payment information.
     *
     * @param orderId the order ID
     * @return the complete order details
     * @throws com.nifilili.core.exception.ResourceNotFoundException if order not found or not owned by current user
     */
    OrderDetailsResponse getOrderDetails(Long orderId);
}
