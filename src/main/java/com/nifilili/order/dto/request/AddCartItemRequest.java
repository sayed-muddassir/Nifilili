package com.nifilili.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddCartItemRequest {

    @NotNull
    private Long offeringId;

    private Long variantId;

    @NotNull
    @Positive
    private Integer quantity;
}
