package com.nifilili.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyPaymentRequest {

    @NotNull
    private Boolean verified;

    private String reference;
}
