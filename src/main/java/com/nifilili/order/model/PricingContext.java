package com.nifilili.order.model;

import com.nifilili.core.dto.AddressDto;
import com.nifilili.order.dto.request.checkout.CouponApplyRequest;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Immutable input to PricingService.
 */
@Getter
@Builder
public class PricingContext {

    private final CartSnapshot cart;

    /**
     * Key = businessId
     */
    private final Map<Long, BusinessConfig> businessConfigs;

    /**
     * Key = businessId
     */
    private final Map<Long, CouponApplyRequest> couponsByBusiness;

    private final AddressDto address;

    private final List<CouponApplyRequest> coupons;

    private final Map<Long, Coupon> validCoupons;
}

