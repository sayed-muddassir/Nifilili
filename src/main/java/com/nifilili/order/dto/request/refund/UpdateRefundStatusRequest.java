package com.nifilili.order.dto.request.refund;

import lombok.Data;

@Data
public class UpdateRefundStatusRequest {
    private String status;
    private String reference;
}

