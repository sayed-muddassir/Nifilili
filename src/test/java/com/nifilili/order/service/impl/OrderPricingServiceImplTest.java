package com.nifilili.order.service.impl;

import com.nifilili.core.enums.util.DiscountType;
import com.nifilili.order.domain.CouponEntity;
import com.nifilili.order.dto.response.CartItemResponse;
import com.nifilili.order.service.PricingService.PricingResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrderPricingServiceImplTest {

    @InjectMocks
    private OrderPricingServiceImpl pricingService;

    // --- calculate ---

    @Test
    void calculate_WhenSingleItemNoCoupon_ShouldCalculateWithDefaultTax() {
        // Item: qty=2, unitPrice=100 → subtotal=200
        // Default tax rate: 13% of 200 = 26
        // No discount, no delivery
        // Payable = 200 + 26 + 0 - 0 = 226
        CartItemResponse item = buildItem(1L, 1L, new BigDecimal("100.00"), 2);
        Map<Long, CouponEntity> coupons = Map.of();
        Map<Long, Map<String, String>> configs = Map.of(1L, Map.of());

        PricingResult result = pricingService.calculate(List.of(item), coupons, configs);

        assertThat(result.subtotal()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(result.totalTax()).isEqualByComparingTo(new BigDecimal("26.00"));
        assertThat(result.totalDelivery()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.payableAmount()).isEqualByComparingTo(new BigDecimal("226.00"));
    }

    @Test
    void calculate_WhenCustomTaxAndDelivery_ShouldUseBusinessConfig() {
        // Item: qty=1, unitPrice=500 → subtotal=500
        // Custom tax rate: 10% of 500 = 50
        // Custom delivery charge: 100
        // Payable = 500 + 50 + 100 - 0 = 650
        CartItemResponse item = buildItem(1L, 1L, new BigDecimal("500.00"), 1);
        Map<Long, CouponEntity> coupons = Map.of();
        Map<Long, Map<String, String>> configs = Map.of(
                1L, Map.of("tax_rate", "10", "delivery_charge", "100"));

        PricingResult result = pricingService.calculate(List.of(item), coupons, configs);

        assertThat(result.subtotal()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.totalTax()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.totalDelivery()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(result.payableAmount()).isEqualByComparingTo(new BigDecimal("650.00"));
    }

    @Test
    void calculate_WhenPercentageCouponApplied_ShouldReduceTaxBase() {
        // Item: qty=1, unitPrice=1000 → subtotal=1000
        // Coupon: 10% → discount=100
        // Tax: 13% of (1000 - 100) = 13% of 900 = 117
        // Payable = 1000 + 117 + 0 - 100 = 1017
        CartItemResponse item = buildItem(1L, 1L, new BigDecimal("1000.00"), 1);
        CouponEntity coupon = buildCoupon(DiscountType.PERCENTAGE, new BigDecimal("10"), null);
        Map<Long, CouponEntity> coupons = Map.of(1L, coupon);
        Map<Long, Map<String, String>> configs = Map.of(1L, Map.of());

        PricingResult result = pricingService.calculate(List.of(item), coupons, configs);

        assertThat(result.totalDiscount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.totalTax()).isEqualByComparingTo(new BigDecimal("117.00"));
        assertThat(result.payableAmount()).isEqualByComparingTo(new BigDecimal("1017.00"));
    }

    @Test
    void calculate_WhenFixedAmountCoupon_ShouldApplyFullDiscount() {
        // Item: qty=1, unitPrice=500 → subtotal=500
        // Coupon: fixed 50 → discount=50
        // Tax: 13% of (500 - 50) = 13% of 450 = 58.50
        // Payable = 500 + 58.50 + 0 - 50 = 508.50
        CartItemResponse item = buildItem(1L, 1L, new BigDecimal("500.00"), 1);
        CouponEntity coupon = buildCoupon(DiscountType.FIXED_AMOUNT, new BigDecimal("50"), null);
        Map<Long, CouponEntity> coupons = Map.of(1L, coupon);
        Map<Long, Map<String, String>> configs = Map.of(1L, Map.of());

        PricingResult result = pricingService.calculate(List.of(item), coupons, configs);

        assertThat(result.totalDiscount()).isEqualByComparingTo(new BigDecimal("50"));
        assertThat(result.totalTax()).isEqualByComparingTo(new BigDecimal("58.50"));
        assertThat(result.payableAmount()).isEqualByComparingTo(new BigDecimal("508.50"));
    }

    @Test
    void calculate_WhenMultipleItems_ShouldAggregateAll() {
        // Item1: business=1, qty=2, price=100 → sub=200
        // Item2: business=2, qty=1, price=300 → sub=300
        // Default tax on both (13%)
        // Item1 tax: 13% of 200 = 26, Item2 tax: 13% of 300 = 39
        // Total tax = 65, Total subtotal = 500
        // Payable = 500 + 65 + 0 - 0 = 565
        CartItemResponse item1 = buildItem(1L, 1L, new BigDecimal("100.00"), 2);
        CartItemResponse item2 = buildItem(2L, 2L, new BigDecimal("300.00"), 1);

        Map<Long, CouponEntity> coupons = Map.of();
        Map<Long, Map<String, String>> configs = Map.of(1L, Map.of(), 2L, Map.of());

        PricingResult result = pricingService.calculate(List.of(item1, item2), coupons, configs);

        assertThat(result.subtotal()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.totalTax()).isEqualByComparingTo(new BigDecimal("65.00"));
        assertThat(result.payableAmount()).isEqualByComparingTo(new BigDecimal("565.00"));
    }

    @Test
    void calculate_WhenPercentageCouponWithMaxDiscount_ShouldCapDiscount() {
        // Item: qty=1, unitPrice=1000 → subtotal=1000
        // Coupon: 20% with max 100 → discount = min(200, 100) = 100
        // Tax: 13% of (1000 - 100) = 117
        // Payable = 1000 + 117 + 0 - 100 = 1017
        CartItemResponse item = buildItem(1L, 1L, new BigDecimal("1000.00"), 1);
        CouponEntity coupon = buildCoupon(DiscountType.PERCENTAGE, new BigDecimal("20"),
                new BigDecimal("100"));
        Map<Long, CouponEntity> coupons = Map.of(1L, coupon);
        Map<Long, Map<String, String>> configs = Map.of(1L, Map.of());

        PricingResult result = pricingService.calculate(List.of(item), coupons, configs);

        assertThat(result.totalDiscount()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(result.payableAmount()).isEqualByComparingTo(new BigDecimal("1017.00"));
    }

    // --- helpers ---

    private CartItemResponse buildItem(Long cartItemId, Long businessId,
                                        BigDecimal unitPrice, int quantity) {
        return CartItemResponse.builder()
                .cartItemId(cartItemId)
                .offeringId(cartItemId * 10)
                .businessId(businessId)
                .title("Item #" + cartItemId)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .available(true)
                .subtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .build();
    }

    private CouponEntity buildCoupon(DiscountType type, BigDecimal value, BigDecimal maxDiscount) {
        return CouponEntity.builder()
                .discountType(type)
                .discountValue(value)
                .maxDiscount(maxDiscount)
                .build();
    }
}
