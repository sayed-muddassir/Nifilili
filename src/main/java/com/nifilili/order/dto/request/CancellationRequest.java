package com.nifilili.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CancellationRequest {

    @Valid
    @NotEmpty
    private List<CancellationItemRequest> items;
}
