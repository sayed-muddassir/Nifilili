package com.nifilili.order.dto.request.checkout;

import lombok.Data;

@Data
public class CouponApplyRequest {
    private Long businessId;
    private String couponCode;
}

