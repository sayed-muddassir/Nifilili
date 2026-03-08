package com.nifilili.quote.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class CreateQuoteRequest {

    @NotBlank
    private String serviceDetails;

    @NotBlank
    private String currency;

    @Positive
    private Integer estimatedDurationDays;

    @NotNull
    @Positive
    private Integer validityDays;

    private List<String> attachments;

    @NotEmpty
    @Valid
    private List<QuoteLineItemRequest> lineItems;
}
