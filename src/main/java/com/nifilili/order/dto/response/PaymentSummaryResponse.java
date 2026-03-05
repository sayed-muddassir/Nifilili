package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentSummaryResponse {
    private String paymentType;
    private String status;
    private BigDecimal amount;
}
