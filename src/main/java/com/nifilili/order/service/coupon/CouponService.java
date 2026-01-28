package com.nifilili.order.service.coupon;

import com.nifilili.order.dto.request.checkout.CouponApplyRequest;
import com.nifilili.order.model.CartSnapshot;
import com.nifilili.order.model.Coupon;

import java.util.List;
import java.util.Map;

public interface CouponService {

    /**
     * Validate coupons against the cart.
     *
     * @return Map keyed by businessId → validated coupon
     */
    Map<Long, Coupon> validateCoupons(
            List<CouponApplyRequest> coupons,
            CartSnapshot cart
    );
}
