package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class PaymentStatusResponse {
    private Long paymentTypeId;
    private String typeName;
    private String status;
    private BigDecimal amount;
    private Map<String, Object> paymentDetails;
}
