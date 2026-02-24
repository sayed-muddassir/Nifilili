package com.nifilili.quote.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuoteLineItemDTO {

    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
}

