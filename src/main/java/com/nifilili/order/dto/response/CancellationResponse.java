package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancellationResponse {
    private Long orderItemId;
    private String status;
    private String reason;
}
