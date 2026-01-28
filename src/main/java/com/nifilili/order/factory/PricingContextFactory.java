package com.nifilili.order.factory;

import com.nifilili.order.dto.request.checkout.CouponApplyRequest;
import com.nifilili.order.dto.request.orderplacement.PlaceOrderRequest;
import com.nifilili.order.model.BusinessConfig;
import com.nifilili.order.model.CartSnapshot;
import com.nifilili.order.model.PricingContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PricingContextFactory is responsible for assembling
 * EVERYTHING required by PricingService into a single object.
 *
 * This keeps PricingService:
 *  - pure
 *  - deterministic
 *  - testable
 *
 * Think of this as a "request adapter" layer.
 */
public final class PricingContextFactory {

    private PricingContextFactory() {
        // Utility class — no instances allowed
    }

    /**
     * Build PricingContext for checkout preview or order placement.
     *
     * @param cartSnapshot immutable snapshot of cart
     * @param request      checkout / place order request
     */
    public static PricingContext from(
            CartSnapshot cartSnapshot,
            PlaceOrderRequest request
    ) {

        // 1️⃣ Extract all business IDs involved in cart
        Set<Long> businessIds = cartSnapshot.getBusinessIds();

        // 2️⃣ Load business configurations (tax, delivery, etc.)
        Map<Long, BusinessConfig> businessConfigs =
                loadBusinessConfigs(businessIds);

        // 3️⃣ Normalize coupon input
        Map<Long, CouponApplyRequest> couponsByBusiness =
                mapCouponsByBusiness(request.getCoupons());

        /**
         * IMPORTANT:
         * PricingContext is a READ-ONLY object.
         * Everything needed for calculation is assembled here.
         */
        return PricingContext.builder()
                .cart(cartSnapshot)
                .businessConfigs(businessConfigs)
                .couponsByBusiness(couponsByBusiness)
                .build();
    }

    // ------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------

    /**
     * Load business configuration for each business.
     *
     * In real implementation this would:
     *  - call Business module
     *  - or query business_configurations table
     *
     * For now, mocked cleanly.
     */
    private static Map<Long, BusinessConfig> loadBusinessConfigs(
            Set<Long> businessIds
    ) {

        Map<Long, BusinessConfig> configs = new HashMap<>();

        for (Long businessId : businessIds) {

            /**
             * Mock defaults.
             * Replace with actual config fetch later.
             */
            BusinessConfig config = BusinessConfig.builder()
                    .businessId(businessId)
                    .taxRate(13)                 // 13%
                    .deliveryCharge(100)         // flat delivery
                    .returnWindowDays(7)
                    .build();

            configs.put(businessId, config);
        }

        return configs;
    }

    /**
     * Convert coupon list into a map keyed by businessId.
     *
     * Rules:
     *  - One coupon per business
     *  - Later duplicates override earlier ones
     */
    private static Map<Long, CouponApplyRequest> mapCouponsByBusiness(
            List<CouponApplyRequest> coupons
    ) {

        Map<Long, CouponApplyRequest> map = new HashMap<>();

        if (coupons == null || coupons.isEmpty()) {
            return map;
        }

        for (CouponApplyRequest coupon : coupons) {
            map.put(coupon.getBusinessId(), coupon);
        }

        return map;
    }
}

