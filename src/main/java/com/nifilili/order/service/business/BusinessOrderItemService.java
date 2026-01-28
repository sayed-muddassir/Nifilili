package com.nifilili.order.service.business;

import com.nifilili.order.dto.request.business.RejectOrderItemRequest;
import com.nifilili.order.dto.request.business.UpdateOrderItemStatusRequest;
import com.nifilili.order.dto.response.business.BusinessOrderItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BusinessOrderItemService {

    /**
     * List order items that belong to the logged-in business.
     * Optional status filter.
     */
    Page<BusinessOrderItemResponse> listItems(String status, Pageable pageable);

    /**
     * Update order item status (received, preparing, shipped, delivered).
     * Creates order_status_history entry.
     */
    void updateStatus(Long orderItemId, UpdateOrderItemStatusRequest request);

    /**
     * Reject an order item (business decision).
     * Final state.
     */
    void reject(Long orderItemId, RejectOrderItemRequest request);
}
