package com.nifilili.order.util;

import com.nifilili.order.model.Coupon;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DiscountCalculator applies coupon discount on an item subtotal.
 *
 * Supported discount types:
 * - PERCENTAGE (with optional max cap)
 * - FIXED amount
 *
 * IMPORTANT:
 * - This class assumes coupon validity is already checked
 * - This class NEVER returns negative values
 */
public final class DiscountCalculator {

    private DiscountCalculator() {
        // Utility class
    }

    /**
     * Calculate discount amount for a single item.
     *
     * @param itemSubtotal subtotal before discount
     * @param coupon       validated coupon (may be null)
     */
    public static BigDecimal calculate(
            BigDecimal itemSubtotal,
            Coupon coupon
    ) {

        if (coupon == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;

        switch (coupon.getDiscountType()) {

            case PERCENTAGE -> {
                discount = itemSubtotal
                        .multiply(coupon.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                // Apply max discount cap if present
                if (coupon.getMaxDiscount() != null &&
                        discount.compareTo(coupon.getMaxDiscount()) > 0) {
                    discount = coupon.getMaxDiscount();
                }
            }

            case FIXED_AMOUNT -> {
                discount = coupon.getDiscountValue();
            }

            default -> discount = BigDecimal.ZERO;
        }

        // Safety: discount cannot exceed subtotal
        if (discount.compareTo(itemSubtotal) > 0) {
            discount = itemSubtotal;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}
