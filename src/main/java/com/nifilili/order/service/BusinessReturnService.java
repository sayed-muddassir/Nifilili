package com.nifilili.order.service;

import com.nifilili.order.dto.request.UpdateReturnStatusRequest;
import com.nifilili.order.dto.response.ReturnResponse;

public interface BusinessReturnService {

    /**
     * Updates the status of a return request through its lifecycle phases:
     * REQUESTED → PICKUP_SCHEDULED → PICKED_UP → RECEIVED → INSPECTED → REFUNDED or REJECTED.
     * On REFUNDED: updates order item status to RETURNED and publishes ReturnApprovedEvent.
     * On REJECTED: requires a rejection reason.
     *
     * @param returnRequestId the return request ID
     * @param request         the new status and optional rejection reason
     * @return the updated return response
     * @throws com.nifilili.core.exception.ResourceNotFoundException  if return request not found
     * @throws com.nifilili.core.exception.InvalidOrderStateException if the status transition is not allowed
     */
    ReturnResponse updateStatus(Long returnRequestId, UpdateReturnStatusRequest request);
}
