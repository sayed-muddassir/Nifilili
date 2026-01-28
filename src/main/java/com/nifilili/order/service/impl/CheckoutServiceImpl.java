package com.nifilili.order.service.impl;

import com.nifilili.order.dto.request.checkout.CheckoutPreviewRequest;
import com.nifilili.order.dto.response.checkout.CheckoutPreviewResponse;
import com.nifilili.order.factory.CheckoutFactory;
import com.nifilili.order.model.CartSnapshot;
import com.nifilili.order.model.PricingContext;
import com.nifilili.order.model.PricingResult;
import com.nifilili.order.service.business.BusinessConfigService;
import com.nifilili.order.service.business.PricingService;
import com.nifilili.order.service.cart.CartService;
import com.nifilili.order.service.checkout.CheckoutService;
import com.nifilili.order.service.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final CartService cartService;
    private final PricingService pricingService;
    private final CouponService couponService;
    private final BusinessConfigService businessConfigService;

    @Override
    public CheckoutPreviewResponse preview(CheckoutPreviewRequest request) {

        CartSnapshot cart = cartService.getValidatedCartSnapshot();

        PricingContext context = PricingContext.builder()
                .cart(cart)
                .address(request.getAddress())
                .coupons(request.getCoupons())
                .businessConfigs(
                        businessConfigService.getConfigs(cart.getBusinessIds())
                )
                .validCoupons(
                        couponService.validateCoupons(request.getCoupons(), cart)
                )
                .build();

        PricingResult pricing = pricingService.calculate(context);

        return CheckoutFactory.toPreviewResponse(pricing);
    }
}

