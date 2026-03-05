package com.nifilili.order.util;

import com.nifilili.core.enums.util.DiscountType;
import com.nifilili.order.domain.CouponEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates discount amount for a given item subtotal and coupon.
 * Supports PERCENTAGE (with optional maxDiscount cap) and FIXED_AMOUNT types.
 */
public final class DiscountCalculator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private DiscountCalculator() {
        // utility class
    }

    /**
     * Calculates the applicable discount.
     *
     * @param itemSubtotal the subtotal before discount
     * @param coupon       the coupon to apply
     * @return the discount amount, never exceeding the subtotal, rounded to 2 decimal places
     */
    public static BigDecimal calculate(BigDecimal itemSubtotal, CouponEntity coupon) {
        if (itemSubtotal == null || itemSubtotal.compareTo(BigDecimal.ZERO) <= 0 || coupon == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;

        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = itemSubtotal
                    .multiply(coupon.getDiscountValue())
                    .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

            // Apply maxDiscount cap if configured
            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        } else {
            // FIXED_AMOUNT
            discount = coupon.getDiscountValue();
        }

        // Discount cannot exceed the subtotal
        if (discount.compareTo(itemSubtotal) > 0) {
            discount = itemSubtotal;
        }

        // Guard against negative discount
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}
