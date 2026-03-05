package com.nifilili.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderItemStatusRequest {

    @NotBlank
    private String newStatus;

    private String note;
}
