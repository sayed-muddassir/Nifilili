package com.nifilili.order.dto.response.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class PaymentStatusResponse {
    private Long paymentTypeId;
    private String status;
    private BigDecimal amount;
    private Map<String, Object> paymentDetails;
}
