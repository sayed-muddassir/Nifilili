package com.nifilili.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRefundStatusRequest {

    @NotBlank
    private String status;

    private String reference;
}
