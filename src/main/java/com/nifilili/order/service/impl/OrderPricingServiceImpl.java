package com.nifilili.order.service.impl;

import com.nifilili.order.domain.CouponEntity;
import com.nifilili.order.dto.response.CartItemResponse;
import com.nifilili.order.service.PricingService;
import com.nifilili.order.util.DiscountCalculator;
import com.nifilili.order.util.TaxCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPricingServiceImpl implements PricingService {

    private static final String CONFIG_TAX_RATE = "tax_rate";
    private static final String CONFIG_DELIVERY_CHARGE = "delivery_charge";
    private static final String DEFAULT_TAX_RATE = "13";
    private static final String DEFAULT_DELIVERY_CHARGE = "0";

    @Override
    public PricingResult calculate(List<CartItemResponse> items,
                                   Map<Long, CouponEntity> coupons,
                                   Map<Long, Map<String, String>> businessConfigs) {
        log.debug("Calculating pricing for {} items", items.size());

        Map<Long, BigDecimal> itemSubtotals = new HashMap<>();
        Map<Long, BigDecimal> itemTaxes = new HashMap<>();
        Map<Long, BigDecimal> itemDelivery = new HashMap<>();
        Map<Long, BigDecimal> itemDiscounts = new HashMap<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalDelivery = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (CartItemResponse item : items) {
            Long itemId = item.getCartItemId();
            Long businessId = item.getBusinessId();
            Map<String, String> config = businessConfigs.getOrDefault(businessId, Map.of());

            // Subtotal
            BigDecimal itemSubtotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            itemSubtotals.put(itemId, itemSubtotal);
            subtotal = subtotal.add(itemSubtotal);

            // Discount
            CouponEntity coupon = coupons.get(businessId);
            BigDecimal discount = DiscountCalculator.calculate(itemSubtotal, coupon);
            itemDiscounts.put(itemId, discount);
            totalDiscount = totalDiscount.add(discount);

            // Tax (on subtotal after discount)
            int taxRate = Integer.parseInt(config.getOrDefault(CONFIG_TAX_RATE, DEFAULT_TAX_RATE));
            BigDecimal taxableAmount = itemSubtotal.subtract(discount);
            BigDecimal tax = TaxCalculator.calculate(taxableAmount, taxRate);
            itemTaxes.put(itemId, tax);
            totalTax = totalTax.add(tax);

            // Delivery charge (per-item, from business config)
            BigDecimal delivery = new BigDecimal(
                    config.getOrDefault(CONFIG_DELIVERY_CHARGE, DEFAULT_DELIVERY_CHARGE));
            itemDelivery.put(itemId, delivery);
            totalDelivery = totalDelivery.add(delivery);
        }

        BigDecimal payableAmount = subtotal
                .add(totalTax)
                .add(totalDelivery)
                .subtract(totalDiscount);

        log.debug("Pricing result: subtotal={}, tax={}, delivery={}, discount={}, payable={}",
                subtotal, totalTax, totalDelivery, totalDiscount, payableAmount);

        return new PricingResult(
                itemSubtotals, itemTaxes, itemDelivery, itemDiscounts,
                subtotal, totalTax, totalDelivery, totalDiscount, payableAmount
        );
    }
}
