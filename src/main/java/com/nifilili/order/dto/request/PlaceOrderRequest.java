package com.nifilili.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PlaceOrderRequest {

    @NotBlank
    private String receiverName;

    @NotBlank
    private String contactNumber;

    @NotBlank
    private String email;

    @Valid
    @NotNull
    private AddressDto address;

    @NotNull
    private Long paymentTypeId;

    private Map<String, Object> paymentDetails;

    private List<@Valid CouponApplyRequest> coupons;

    private String customerNotes;
}
