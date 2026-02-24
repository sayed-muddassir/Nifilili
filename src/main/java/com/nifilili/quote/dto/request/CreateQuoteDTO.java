package com.nifilili.quote.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateQuoteDTO {

    @NotBlank
    private String serviceDetails;

    @NotEmpty
    private List<QuoteLineItemDTO> lineItems;

    @NotNull
    private BigDecimal totalAmount;

    private String currency = "INR";

    private Integer estimatedDurationDays;

    private LocalDateTime validUntil;

    private List<String> attachments;
}

