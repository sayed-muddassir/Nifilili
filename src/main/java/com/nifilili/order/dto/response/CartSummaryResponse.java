package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartSummaryResponse {
    private BigDecimal subtotal;
    private List<String> warnings;
}
