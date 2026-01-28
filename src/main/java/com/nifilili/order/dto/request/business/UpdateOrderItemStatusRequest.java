package com.nifilili.order.dto.request.business;

import lombok.Data;

@Data
public class UpdateOrderItemStatusRequest {
    private String newStatus;
    private String note;
}
