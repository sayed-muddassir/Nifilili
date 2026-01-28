package com.nifilili.order.dto.request.cancellation;

import lombok.Data;

@Data
public class CancellationDecisionRequest {
    private Boolean approved;
    private String reason;
}

