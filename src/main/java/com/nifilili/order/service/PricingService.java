package com.nifilili.order.service;

import com.nifilili.order.domain.CouponEntity;
import com.nifilili.order.dto.response.CartItemResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PricingService {

    /**
     * Holds the computed pricing result for the entire order.
     *
     * @param itemSubtotals   per-item subtotal (quantity * unitPrice)
     * @param itemTaxes       per-item tax amount
     * @param itemDelivery    per-item delivery charge
     * @param itemDiscounts   per-item discount amount
     * @param subtotal        sum of all item subtotals
     * @param totalTax        sum of all item taxes
     * @param totalDelivery   total delivery charge
     * @param totalDiscount   total discount amount
     * @param payableAmount   final payable amount (subtotal + tax + delivery - discount)
     */
    record PricingResult(
            Map<Long, BigDecimal> itemSubtotals,
            Map<Long, BigDecimal> itemTaxes,
            Map<Long, BigDecimal> itemDelivery,
            Map<Long, BigDecimal> itemDiscounts,
            BigDecimal subtotal,
            BigDecimal totalTax,
            BigDecimal totalDelivery,
            BigDecimal totalDiscount,
            BigDecimal payableAmount
    ) {}

    /**
     * Calculates pricing for all items in the cart: per-item subtotals, taxes, delivery charges,
     * and discounts, plus aggregated totals.
     *
     * @param items         the enriched cart items with unit prices
     * @param coupons       map of business ID to validated coupon entity (nullable values for no coupon)
     * @param businessConfigs map of business ID to their config key-value pairs (tax_rate, delivery_charge)
     * @return the complete pricing result
     */
    PricingResult calculate(List<CartItemResponse> items,
                            Map<Long, CouponEntity> coupons,
                            Map<Long, Map<String, String>> businessConfigs);
}
