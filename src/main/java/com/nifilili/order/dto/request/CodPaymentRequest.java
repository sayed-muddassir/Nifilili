package com.nifilili.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CodPaymentRequest {

    @NotNull
    private BigDecimal amountReceived;

    private String receiverName;
}
