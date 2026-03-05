package com.nifilili.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateCouponRequest {

    @NotBlank
    private String code;

    @NotNull
    private String discountType;

    @NotNull
    private BigDecimal discountValue;

    private BigDecimal maxDiscount;

    private BigDecimal minOrderAmount;

    @NotNull
    private LocalDate validFrom;

    @NotNull
    private LocalDate validTo;
}
