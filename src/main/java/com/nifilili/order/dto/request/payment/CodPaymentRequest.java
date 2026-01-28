package com.nifilili.order.dto.request.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CodPaymentRequest {
    private BigDecimal amountReceived;
    private String receiverName;
    private String deliveryAgent;
}

