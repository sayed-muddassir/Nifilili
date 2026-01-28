package com.nifilili.order.service.impl;

import com.nifilili.core.enums.util.DiscountType;
import com.nifilili.order.dto.request.checkout.CouponApplyRequest;
import com.nifilili.order.model.CartItemSnapshot;
import com.nifilili.order.model.CartSnapshot;
import com.nifilili.order.model.Coupon;
import com.nifilili.order.service.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * CouponService implementation.
 *
 * NOTE:
 * This is intentionally implemented as a clean, mock version.
 * Replacing it with DB-backed coupons later requires NO API change.
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    @Override
    public Map<Long, Coupon> validateCoupons(
            List<CouponApplyRequest> coupons,
            CartSnapshot cart
    ) {

        Map<Long, Coupon> validCoupons = new HashMap<>();

        if (coupons == null || coupons.isEmpty()) {
            return validCoupons;
        }

        for (CouponApplyRequest request : coupons) {

            Long businessId = request.getBusinessId();

            // 🚫 Coupon business must exist in cart
            if (!cart.getBusinessIds().contains(businessId)) {
                continue;
            }

            Coupon coupon = fetchCoupon(request.getCouponCode(), businessId);

            // 🚫 Coupon not found or inactive
            if (coupon == null || !coupon.isActive()) {
                continue;
            }

            // 🚫 Expiry check
            LocalDate today = LocalDate.now();
            if (today.isBefore(coupon.getValidFrom()) ||
                    today.isAfter(coupon.getValidTo())) {
                continue;
            }

            // 🚫 Minimum order amount per business
            BigDecimal businessSubtotal =
                    calculateBusinessSubtotal(cart, businessId);

            if (businessSubtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                continue;
            }

            // ✅ Coupon is valid
            validCoupons.put(businessId, coupon);
        }

        return validCoupons;
    }

    // ------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------

    /**
     * Mock coupon fetch.
     * Replace with DB lookup later.
     */
    private Coupon fetchCoupon(String code, Long businessId) {

        // Mocked coupon for demo
        if (!"SAVE10".equalsIgnoreCase(code)) {
            return null;
        }

        return Coupon.builder()
                .businessId(businessId)
                .code(code)
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10))   // 10%
                .maxDiscount(BigDecimal.valueOf(200))
                .minOrderAmount(BigDecimal.valueOf(500))
                .validFrom(LocalDate.now().minusDays(1))
                .validTo(LocalDate.now().plusDays(10))
                .active(true)
                .build();
    }

    /**
     * Calculate subtotal for a specific business.
     */
    private BigDecimal calculateBusinessSubtotal(
            CartSnapshot cart,
            Long businessId
    ) {

        return cart.getItems().stream()
                .filter(i -> i.getBusinessId().equals(businessId))
                .map(CartItemSnapshot::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

