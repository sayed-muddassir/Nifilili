package com.nifilili.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CouponApplyRequest {

    @NotNull
    private Long businessId;

    @NotBlank
    private String couponCode;
}
