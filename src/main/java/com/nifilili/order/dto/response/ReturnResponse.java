package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReturnResponse {
    private Long returnRequestId;
    private String rmaNumber;
    private String status;
}
