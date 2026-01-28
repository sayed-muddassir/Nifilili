package com.nifilili.order.service.impl;

import com.nifilili.order.model.*;
import com.nifilili.order.service.business.PricingService;
import com.nifilili.order.util.DiscountCalculator;
import com.nifilili.order.util.TaxCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PricingServiceImpl implements PricingService {

    @Override
    public PricingResult calculate(PricingContext ctx) {

        List<PricingItem> pricingItems = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal deliveryCharge = BigDecimal.ZERO;

        for (CartItemSnapshot item : ctx.getCart().getItems()) {

            BusinessConfig config = ctx.getBusinessConfigs()
                    .get(item.getBusinessId());

            BigDecimal itemSubtotal =
                    item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

//            BigDecimal discount =
//                    DiscountCalculator.calculate(itemSubtotal,
//                            ctx.getValidCoupons().get(item.getBusinessId()));
            BigDecimal discount = BigDecimal.ZERO;// TODO: Coupon integration

            BigDecimal taxableAmount = itemSubtotal.subtract(discount);

            BigDecimal tax =
                    TaxCalculator.calculate(taxableAmount, config.getTaxRate());

            BigDecimal delivery =
                    BigDecimal.valueOf(config.getDeliveryCharge());

            pricingItems.add(
                    PricingItem.builder()
                            .offeringId(item.getOfferingId())
                            .businessId(item.getBusinessId())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .subTotal(itemSubtotal)
                            .discount(discount)
                            .tax(tax)
                            .deliveryCharge(delivery)
                            .build()
            );

            subtotal = subtotal.add(itemSubtotal);
            totalDiscount = totalDiscount.add(discount);
            totalTax = totalTax.add(tax);
            deliveryCharge = deliveryCharge.add(delivery);
        }

        PricingSummary summary = PricingSummary.builder()
                .subTotal(subtotal)
                .totalDiscount(totalDiscount)
                .totalTax(totalTax)
                .deliveryCharge(deliveryCharge)
                .payableAmount(
                        subtotal
                                .add(totalTax)
                                .add(deliveryCharge)
                                .subtract(totalDiscount)
                )
                .build();

        return new PricingResult(pricingItems, summary);
    }
}

