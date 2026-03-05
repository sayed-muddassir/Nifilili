package com.nifilili.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectOrderItemRequest {

    @NotBlank
    private String reason;
}
