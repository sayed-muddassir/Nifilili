package com.nifilili.order.service.returnflow;

import com.nifilili.order.dto.request.returnflow.CreateReturnRequest;
import com.nifilili.order.dto.response.returnflow.ReturnResponse;

public interface ReturnService {

    /**
     * User requests return for a delivered order item.
     */
    ReturnResponse createReturn(Long orderItemId, CreateReturnRequest request);
}
