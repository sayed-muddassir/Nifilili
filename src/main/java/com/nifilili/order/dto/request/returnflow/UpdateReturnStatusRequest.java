package com.nifilili.order.dto.request.returnflow;

import lombok.Data;

@Data
public class UpdateReturnStatusRequest {

    /**
     * requested → pickup_scheduled → picked_up → received → inspected → refunded / rejected
     */
    private String status;

    /**
     * Mandatory only when status = rejected
     */
    private String rejectionReason;
}
