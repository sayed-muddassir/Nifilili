package com.nifilili.order.util;

import com.nifilili.core.enums.util.DiscountType;
import com.nifilili.order.domain.CouponEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscountCalculatorTest {

    @Test
    void calculate_WhenPercentageDiscount_ShouldReturnCorrectAmount() {
        CouponEntity coupon = CouponEntity.builder()
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .build();

        BigDecimal result = DiscountCalculator.calculate(new BigDecimal("1000.00"), coupon);
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void calculate_WhenPercentageWithMaxDiscount_ShouldCapAtMax() {
        CouponEntity coupon = CouponEntity.builder()
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("50"))
                .maxDiscount(new BigDecimal("200.00"))
                .build();

        BigDecimal result = DiscountCalculator.calculate(new BigDecimal("1000.00"), coupon);
        assertEquals(new BigDecimal("200.00"), result);
    }

    @Test
    void calculate_WhenFixedAmountDiscount_ShouldReturnFixedValue() {
        CouponEntity coupon = CouponEntity.builder()
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("150.00"))
                .build();

        BigDecimal result = DiscountCalculator.calculate(new BigDecimal("1000.00"), coupon);
        assertEquals(new BigDecimal("150.00"), result);
    }

    @Test
    void calculate_WhenFixedAmountExceedsSubtotal_ShouldReturnSubtotal() {
        CouponEntity coupon = CouponEntity.builder()
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("500.00"))
                .build();

        BigDecimal result = DiscountCalculator.calculate(new BigDecimal("200.00"), coupon);
        assertEquals(new BigDecimal("200.00"), result);
    }

    @Test
    void calculate_WhenNullCoupon_ShouldReturnZero() {
        BigDecimal result = DiscountCalculator.calculate(new BigDecimal("1000.00"), null);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void calculate_WhenNullSubtotal_ShouldReturnZero() {
        CouponEntity coupon = CouponEntity.builder()
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .build();

        BigDecimal result = DiscountCalculator.calculate(null, coupon);
        assertEquals(BigDecimal.ZERO, result);
    }
}
