package com.nifilili.quote.dto.request;

import com.nifilili.core.dto.AddressDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateQuoteRequestRequest {

    @NotNull
    private Long offeringId;

    @NotNull
    private Long businessId;

    @NotBlank
    private String requirements;

    private String budgetRange;

    private LocalDate preferredTimeline;

    @Valid
    private AddressDto deliveryAddress;

    private List<String> attachments;
}
