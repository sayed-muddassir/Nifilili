package com.nifilili.order.dto.request.cancellation;

import lombok.Data;

@Data
public class CancellationItemRequest {
    private Long orderItemId;
    private String reason;
    private String note;
}

