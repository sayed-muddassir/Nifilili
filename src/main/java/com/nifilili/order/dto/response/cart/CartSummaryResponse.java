package com.nifilili.order.dto.response.cart;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartSummaryResponse {
    private BigDecimal subtotal;
    private List<String> warnings;
}

