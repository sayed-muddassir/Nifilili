package com.nifilili.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateReturnRequest {

    @NotBlank
    private String reason;

    private String details;

    private List<String> photos;

    @Valid
    @NotNull
    private AddressDto pickupAddress;
}
