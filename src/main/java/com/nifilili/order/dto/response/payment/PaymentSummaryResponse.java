package com.nifilili.order.dto.response.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentSummaryResponse {
    private String paymentType;
    private String status;
    private BigDecimal amount;
}

