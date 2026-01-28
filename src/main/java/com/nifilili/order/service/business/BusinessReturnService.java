package com.nifilili.order.service.business;

import com.nifilili.order.dto.request.returnflow.UpdateReturnStatusRequest;

public interface BusinessReturnService {

    /**
     * Update return request status.
     * This controls the entire return lifecycle from business side.
     */
    void updateStatus(Long returnRequestId, UpdateReturnStatusRequest request);
}
