package com.nifilili.order.service;

import com.nifilili.order.dto.request.CreateReturnRequest;
import com.nifilili.order.dto.response.ReturnResponse;

public interface ReturnService {

    /**
     * Creates a return request for a delivered order item. Validates that the item is delivered,
     * the return window has not expired, and no existing return request exists.
     *
     * @param orderItemId the order item ID to return
     * @param request     the return reason, details, photos, and pickup address
     * @return the created return response with RMA number
     * @throws com.nifilili.core.exception.ResourceNotFoundException  if order item not found
     * @throws com.nifilili.core.exception.InvalidOrderStateException if item is not delivered or return window expired
     */
    ReturnResponse createReturn(Long orderItemId, CreateReturnRequest request);
}
