package com.nifilili.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CheckoutPreviewRequest {

    @Valid
    @NotNull
    private AddressDto address;

    private List<@Valid CouponApplyRequest> coupons;
}
