package com.nifilili.order.dto.request.payment;

import lombok.Data;

@Data
public class VerifyPaymentRequest {
    private Boolean verified;
    private String reference;
}

