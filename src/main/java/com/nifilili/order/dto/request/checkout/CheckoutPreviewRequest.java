package com.nifilili.order.dto.request.checkout;

import com.nifilili.core.dto.AddressDto;
import lombok.Data;

import java.util.List;

@Data
public class CheckoutPreviewRequest {
    private AddressDto address;
    private List<CouponApplyRequest> coupons;
}

