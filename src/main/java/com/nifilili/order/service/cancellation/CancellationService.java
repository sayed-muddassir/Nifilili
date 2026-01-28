package com.nifilili.order.service.cancellation;

import com.nifilili.order.dto.request.cancellation.CancellationDecisionRequest;
import com.nifilili.order.dto.request.cancellation.CancellationRequest;

public interface CancellationService {

    /**
     * User requests cancellation for one or more order items.
     * This only creates cancellation intent – no state change yet.
     */
    void requestCancellation(Long orderId, CancellationRequest request);

    /**
     * Business decides on a cancellation request (approve / reject).
     * This changes order item status.
     */
    void decide(Long orderItemId, CancellationDecisionRequest request);
}
